#!/usr/bin/env bash
# Smoke-test copilot Wasm deployment.
# Usage: ./scripts/smoke-copilot-web.sh [base_url]
# Example: ./scripts/smoke-copilot-web.sh https://copilot.masterdoc.pro
set -euo pipefail

BASE="${1:-https://copilot.masterdoc.pro}"
BASE="${BASE%/}"
API_BASE="${MASTERDOC_API_BASE:-https://api.masterdoc.pro}"
API_BASE="${API_BASE%/}"

echo "==> GET ${BASE}/"
HTML="$(curl -fsS --max-time 30 "${BASE}/")"
echo "${HTML}" | head -c 300
echo
echo "${HTML}" | grep -qi masterdoc || { echo "FAIL: HTML missing Masterdoc"; exit 1; }

echo "==> GET ${BASE}/composeApp.js"
JS="$(curl -fsS --max-time 60 "${BASE}/composeApp.js")"
echo "composeApp.js: $(echo -n "${JS}" | wc -c) bytes"

WASM_FILES="$(echo "${JS}" | grep -oE '[a-f0-9]{20}\.wasm' | sort -u)"
if [[ -z "${WASM_FILES}" ]]; then
  echo "FAIL: no hashed .wasm references in composeApp.js"
  exit 1
fi

while IFS= read -r wasm; do
  [[ -n "${wasm}" ]] || continue
  echo "==> HEAD ${BASE}/${wasm}"
  code="$(curl -fsS -o /dev/null -w "%{http_code}" --max-time 120 "${BASE}/${wasm}")"
  [[ "${code}" == "200" ]] || { echo "FAIL: ${wasm} HTTP ${code}"; exit 1; }
  echo "${wasm}: HTTP ${code}"
done <<< "${WASM_FILES}"

echo "==> GET ${API_BASE}/health"
curl -fsS --max-time 15 "${API_BASE}/health"
echo

echo "OK"
