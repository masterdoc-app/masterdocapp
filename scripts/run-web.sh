#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PKG="$ROOT/build/js/packages/Masterdoc-composeApp-wasm-js"
PORT="${MASTERDOC_WEB_PORT:-8088}"

cd "$ROOT"
# API URL is baked into Wasm at compile time — always regenerate from local.properties.
./gradlew :shared:generateMasterdocBuildConfig :composeApp:compileKotlinWasmJs :composeApp:wasmJsBrowserDevelopmentWebpack --no-daemon -q
cd "$PKG"
rm -f kotlin/composeApp.js
npx webpack --config webpack.config.js --output-path ./kotlin

if command -v fuser >/dev/null 2>&1; then
  fuser -k "${PORT}/tcp" >/dev/null 2>&1 || true
elif command -v lsof >/dev/null 2>&1; then
  pid="$(lsof -ti ":${PORT}" 2>/dev/null || true)"
  if [ -n "$pid" ]; then kill $pid 2>/dev/null || true; sleep 1; fi
else
  pkill -f "http.server ${PORT}" 2>/dev/null || true
fi

echo "Web: http://127.0.0.1:${PORT}/"
echo "API: http://api.masterdoc.pro/v1 (local.properties masterdoc.api.baseUrl)"
exec python3 -m http.server "$PORT" --directory "$PKG/kotlin"
