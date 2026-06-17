#!/usr/bin/env bash
# Verify backend saved a case report (journal line from CaseReportsRepository).
# Usage:
#   ./scripts/verify-e2e-report-log.sh MARKER
#   MASTERDOC_E2E_SERVER_LOG_CMD='journalctl -u masterdoc-backend --since "20 min ago"' \
#     ./scripts/verify-e2e-report-log.sh e2e-flow-1717420000000
#
# Remote VPS example:
#   MASTERDOC_E2E_SERVER_LOG_CMD='ssh root@api.masterdoc.pro journalctl -u masterdoc-backend --since "20 min ago"' \
#     ./scripts/verify-e2e-report-log.sh "$MARKER"
set -euo pipefail

MARKER="${1:?marker required, e.g. e2e-flow-1717420000000}"
LOG_CMD="${MASTERDOC_E2E_SERVER_LOG_CMD:-journalctl -u masterdoc-backend --since \"20 min ago\"}"

echo "==> Searching logs for case-report + marker=$MARKER"
# shellcheck disable=SC2086
OUT="$(bash -lc "$LOG_CMD 2>/dev/null | grep -F 'case-report' | grep -F '$MARKER'" || true)"
if [[ -z "$OUT" ]]; then
  echo "FAIL: no [fixaverse case-report] line with marker in server logs"
  echo "Hint: run E2E with MASTERDOC_INTEGRATION=1, then grep on the host running backend"
  exit 1
fi
echo "$OUT"
echo "OK"
