#!/usr/bin/env bash
# Run ON THE SERVER (root@80.87.196.33) when load is high / Onyx hangs.
set -euo pipefail
cd /root/onyx_data/deployment

echo "=== BEFORE ==="
uptime
free -h

echo "=== Stop heavy Onyx orphans (NOT in Lite) ==="
docker stop \
  onyx-indexing_model_server-1 \
  onyx-inference_model_server-1 \
  onyx-opensearch-1 \
  onyx-index-1 \
  onyx-cache-1 \
  onyx-minio-1 \
  onyx-vespa-1 \
  2>/dev/null || true

echo "=== Lite stack only ==="
docker compose -f docker-compose.yml -f docker-compose.onyx-lite.yml down --remove-orphans 2>/dev/null || true
docker compose -f docker-compose.yml -f docker-compose.onyx-lite.yml up -d

echo "=== Wait for API (up to 3 min) ==="
for i in $(seq 1 18); do
  st=$(docker inspect -f '{{.State.Health.Status}}' onyx-api_server-1 2>/dev/null || echo missing)
  echo "try $i: $st"
  if [[ "$st" == "healthy" ]]; then
    curl -sS --max-time 3 http://127.0.0.1:3000/api/health && echo && break
  fi
  sleep 10
done

echo "=== Containers ==="
docker ps --format 'table {{.Names}}\t{{.Status}}' | grep onyx || docker ps

echo "=== API logs (last 30 lines) ==="
docker logs onyx-api_server-1 --tail 30 2>&1 || true

echo "=== AFTER ==="
uptime
free -h
