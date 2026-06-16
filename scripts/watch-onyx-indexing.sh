#!/usr/bin/env bash
# Watch Onyx indexing for Холодильники-доки (and queue). Logs to scripts/onyx-index-watch.log
set -u
HOST="80.87.196.33"
LOG_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG="${LOG_DIR}/onyx-index-watch.log"
INTERVAL="${WATCH_INTERVAL_SEC:-45}"
MAX_ROUNDS="${WATCH_MAX_ROUNDS:-80}"

log() { echo "[$(date -Iseconds)] $*" | tee -a "$LOG"; }

remote_check() {
  ssh -o BatchMode=yes -o ConnectTimeout=18 -o ConnectionAttempts=1 "root@${HOST}" '
set -e
echo "--- memory ---"
free -h | head -2
echo "--- index attempts (id>=6) ---"
docker exec onyx-relational_db-1 psql -U postgres -d postgres -c "
SELECT ia.id, c.name, ia.status, ia.new_docs_indexed, ia.total_docs_indexed,
       LEFT(COALESCE(ia.error_msg,'\'''\''), 100) AS err
FROM index_attempt ia
JOIN connector_credential_pair ccp ON ccp.id = ia.connector_credential_pair_id
JOIN connector c ON c.id = ccp.connector_id
WHERE ia.id >= 6 ORDER BY ia.id;
"
echo "--- connector pair status ---"
docker exec onyx-relational_db-1 psql -U postgres -d postgres -c "
SELECT c.name, ccp.status, ccp.last_successful_index_time
FROM connector c
JOIN connector_credential_pair ccp ON ccp.connector_id = c.id
WHERE c.name IN ('\''Холодильники-доки'\'','\''Атлант-холодильники'\'','\''Атлант-стиралки'\'');
"
echo "--- docfetching tail ---"
docker exec onyx-background-1 grep -E "Index Attempt: 7|CC Pair: 5|Холодильник|Docfetching finished" /var/log/celery_worker_docfetching.log 2>/dev/null | tail -6 || true
'
}

log "=== watch start host=${HOST} interval=${INTERVAL}s max=${MAX_ROUNDS} ==="

for ((round=1; round<=MAX_ROUNDS; round++)); do
  log "--- round ${round}/${MAX_ROUNDS} ---"

  if ! ping -c 1 -W 2 "$HOST" &>/dev/null; then
    log "ping: fail"
    sleep "$INTERVAL"
    continue
  fi
  log "ping: ok"

  health=$(curl -sS -m 15 -o /tmp/onyx-h.json -w "%{http_code}" "http://${HOST}:3000/api/health" 2>/tmp/onyx-h.err || echo "000")
  if [[ "$health" == "200" ]]; then
    log "api health: HTTP 200"
  else
    err=$(head -c 50 /tmp/onyx-h.err 2>/dev/null | tr -d '\n')
    log "api health: HTTP ${health} ${err}"
  fi

  if remote_check >>"$LOG" 2>&1; then
    log "ssh: ok (details above)"
  else
    log "ssh: fail or timeout"
  fi

  if tail -40 "$LOG" | grep -qE '\|\s*7\s+\|.*SUCCESS'; then
    log "=== SUCCESS: attempt 7 SUCCESS — stopping watch ==="
    exit 0
  fi
  if tail -40 "$LOG" | grep -q 'Холодильники-доки.*ACTIVE'; then
    log "=== SUCCESS: connector ACTIVE — stopping watch ==="
    exit 0
  fi

  sleep "$INTERVAL"
done

log "=== watch end (max rounds) ==="
