#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PKG="$ROOT/build/js/packages/Masterdoc-composeApp-wasm-js"
PORT="${MASTERDOC_WEB_PORT:-8088}"

cd "$ROOT"
# API URL is baked into Wasm at compile time — always regenerate from local.properties.
./gradlew :shared:generateMasterdocBuildConfig :composeApp:compileDevelopmentExecutableKotlinWasmJs :composeApp:wasmJsBrowserDevelopmentWebpack --no-daemon -q
cd "$PKG"
cp "$ROOT/composeApp/src/wasmJsMain/resources/index.html" \
   "$ROOT/composeApp/src/wasmJsMain/resources/masterdoc-camera.js" \
   "$ROOT/composeApp/src/wasmJsMain/resources/masterdoc-image.js" \
   "$PKG/kotlin/"

# Patch before webpack so composeApp.js bundle includes Kotlin error logging.
UNINST="$PKG/kotlin/Masterdoc-composeApp-wasm-js.uninstantiated.mjs"
if [ -f "$UNINST" ]; then
  python3 - "$UNINST" <<'PY'
import sys
path = sys.argv[1]
text = open(path, encoding="utf-8").read()
needle = "'kotlin.wasm.internal.throwJsError' : (message, wasmTypeName, stack) => { \n            const error = new Error();"
insert = "'kotlin.wasm.internal.throwJsError' : (message, wasmTypeName, stack) => { \n            console.error('[masterdoc kotlin]', wasmTypeName, message, stack);\n            try { window.__masterdocLastKotlinError = wasmTypeName + ': ' + message + '\\n' + (stack || ''); } catch (e) {}\n            const error = new Error();"
if needle in text and insert not in text:
    text = text.replace(needle, insert, 1)
    open(path, "w", encoding="utf-8").write(text)
PY
fi

npx webpack --config webpack.config.js --output-path ./kotlin
cp "$ROOT/composeApp/src/wasmJsMain/resources/index.html" \
   "$ROOT/composeApp/src/wasmJsMain/resources/masterdoc-camera.js" \
   "$ROOT/composeApp/src/wasmJsMain/resources/masterdoc-image.js" \
   "$PKG/kotlin/"

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
