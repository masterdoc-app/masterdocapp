#!/usr/bin/env bash
# Periodic Onyx server monitor. Logs to scripts/onyx-monitor.log
set -u
HOST="80.87.196.33"
API="http://${HOST}:3000/api/health"
LOG_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG="${LOG_DIR}/onyx-monitor.log"
INTERVAL="${MONITOR_INTERVAL_SEC:-90}"
MAX_ROUNDS="${MONITOR_MAX_ROUNDS:-40}"

log() { echo "[$(date -Iseconds)] $*" | tee -a "$LOG"; }

log "=== monitor start host=${HOST} interval=${INTERVAL}s max_rounds=${MAX_ROUNDS} ==="

for ((round=1; round<=MAX_ROUNDS; round++)); do
  log "--- round ${round}/${MAX_ROUNDS} ---"

  if ping -c 1 -W 2 "$HOST" &>/dev/null; then
    log "ping: ok"
  else
    log "ping: fail"
  fi

  http_code=$(curl -sS -o /tmp/onyx-health.json -w "%{http_code}" -m 8 "$API" 2>/tmp/onyx-curl.err || echo "000")
  if [[ "$http_code" == "200" ]]; then
    body=$(head -c 120 /tmp/onyx-health.json 2>/dev/null | tr -d '\n')
    log "health: HTTP ${http_code} ${body}"
  else
    err=$(head -c 80 /tmp/onyx-curl.err 2>/dev/null | tr -d '\n')
    log "health: HTTP ${http_code} err=${err:-timeout}"
  fi

  if ssh -o BatchMode=yes -o ConnectTimeout=12 -o ConnectionAttempts=1 \
    "root@${HOST}" 'uptime; free -h | head -2; docker ps --format "{{.Names}} {{.Status}}" 2>/dev/null | grep onyx | head -8' \
    >>"$LOG" 2>&1; then
    log "ssh: ok (see lines above for uptime/docker)"
  else
    log "ssh: fail or timeout"
  fi

  sleep "$INTERVAL"
done

log "=== monitor end ==="
