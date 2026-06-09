#!/usr/bin/env bash
# Purge junk case reports from the knowledge-base API database.
# Requires backend with DELETE /v1/report/short and DELETE /v1/report/{id}.
set -euo pipefail

API_BASE="${MASTERDOC_API_BASE_URL:-https://api.masterdoc.pro/v1}"
MIN_LENGTH="${MASTERDOC_REPORT_MIN_LENGTH:-21}"

echo "API: $API_BASE"

echo "1) Removing reports with result length < $MIN_LENGTH ..."
if ! curl -fsS -X DELETE "${API_BASE}/report/short?min_length=${MIN_LENGTH}"; then
  echo "   (skip: DELETE /report/short not available — deploy backend or purge SQLite on VPS)"
fi
echo

echo "2) Removing test markers by id ..."
python3 - "$API_BASE" <<'PY'
import json
import sys
import urllib.request

api_base = sys.argv[1]
prefixes = (
    "jvm-report-",
    "e2e-flow-",
    "browser-fetch-",
    "agent-web-e2e-",
    "copilot-verify-",
)
exact = {
    "smoke test report",
    "prod smoke",
    "Тестовый отчёт",
    "Тест ручной прод.",
    "integration-test",
}

def fetch_reports(assistant_id: int) -> list[dict]:
    url = f"{api_base}/report?assistant_id={assistant_id}&page=0&size=100"
    with urllib.request.urlopen(url) as resp:
        return json.load(resp).get("items", [])

def should_delete(result: str) -> bool:
    text = result.strip()
    if text in exact:
        return True
    return any(text.startswith(p) for p in prefixes)

removed = 0
for assistant_id in (1, 2, 3, 4, 5):
    for item in fetch_reports(assistant_id):
        if not should_delete(item.get("result", "")):
            continue
        req = urllib.request.Request(
            f"{api_base}/report/{item['id']}",
            method="DELETE",
        )
        try:
            with urllib.request.urlopen(req) as resp:
                if resp.status in (200, 204):
                    removed += 1
                    print("deleted", item["id"][:8], item["result"][:60])
        except urllib.error.HTTPError as e:
            if e.code == 404:
                print("   (skip: DELETE /report/{id} not available)")
                break
            raise

print(f"test markers removed: {removed}")
PY

echo
echo "Remaining reports:"
for assistant_id in 1 2; do
  echo "--- assistant_id=$assistant_id ---"
  curl -fsS "${API_BASE}/report?assistant_id=${assistant_id}&page=0&size=50" \
    | python3 -c "
import sys, json
d = json.load(sys.stdin)
print('total:', d.get('total', 0))
for i in d.get('items', []):
    print(i['id'][:8], len(i['result']), i['result'][:70])
" || true
done
