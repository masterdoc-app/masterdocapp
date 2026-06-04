#!/usr/bin/env bash
# Serve already-built Wasm assets (no Gradle/Webpack). Use after run-web.sh or when the server was Killed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PKG="$ROOT/build/js/packages/Masterdoc-composeApp-wasm-js/kotlin"
PORT="${MASTERDOC_WEB_PORT:-8088}"

if [ ! -f "$PKG/index.html" ]; then
  echo "Нет сборки в $PKG — сначала: ./scripts/run-web.sh" >&2
  exit 1
fi

# shellcheck source=scripts/web-lib.sh
source "$(dirname "$0")/web-lib.sh"
masterdoc_web_stop_listener "$PORT"

echo "Web: http://127.0.0.1:${PORT}/"
echo "Остановка: Ctrl+C"
exec python3 -m http.server "$PORT" --bind 127.0.0.1 --directory "$PKG"
