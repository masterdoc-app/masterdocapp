#!/usr/bin/env bash
# Run on the Onyx server (or when API responds). Uses PAT from env ONYX_PAT.
set -euo pipefail

BASE_URL="${ONYX_BASE_URL:-http://127.0.0.1:3000/api}"
PAT="${ONYX_PAT:?Set ONYX_PAT (onyx_pat_... admin token)}"

# 30 days — minimum load on a small VPS
REFRESH=2592000
PRUNE=2592000

echo "Listing connectors from ${BASE_URL} ..."
status_json=$(curl -sS --max-time 30 "${BASE_URL}/manage/admin/connector/status" \
  -H "Authorization: Bearer ${PAT}" -H "Content-Type: application/json")

python3 - "$status_json" "$BASE_URL" "$PAT" "$REFRESH" "$PRUNE" <<'PY'
import json, os, sys, urllib.request

statuses = json.loads(sys.argv[1])
base, pat, refresh, prune = sys.argv[2:6]

def call(method, path, body=None):
    url = base.rstrip("/") + path
    data = json.dumps(body).encode() if body else None
    req = urllib.request.Request(
        url, data=data, method=method,
        headers={"Authorization": f"Bearer {pat}", "Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req, timeout=60) as r:
        return r.read().decode()

if not statuses:
    print("No connectors.")
    sys.exit(0)

ok = 0
for item in statuses:
    c = item["connector"]
    cid = c["id"]
    payload = {
        "name": c["name"],
        "source": c["source"],
        "input_type": c["input_type"],
        "connector_specific_config": c["connector_specific_config"],
        "refresh_freq": int(refresh),
        "prune_freq": int(prune),
        "indexing_start": c.get("indexing_start"),
        "access_type": item["access_type"],
        "groups": item.get("groups") or [],
    }
    try:
        call("PATCH", f"/manage/admin/connector/{cid}", payload)
        print(f"OK id={cid} name={c['name']} -> refresh/prune {int(refresh)//86400}d")
        ok += 1
    except Exception as e:
        print(f"FAIL id={cid} name={c['name']}: {e}", file=sys.stderr)

print(f"Updated {ok}/{len(statuses)}")
sys.exit(0 if ok == len(statuses) else 2)
PY
