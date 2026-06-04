#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PKG="$ROOT/build/js/packages/Masterdoc-composeApp-wasm-js"
PORT="${MASTERDOC_WEB_PORT:-8088}"
SKIP_BUILD="${MASTERDOC_WEB_SKIP_BUILD:-0}"

# shellcheck source=scripts/web-lib.sh
source "$(dirname "$0")/web-lib.sh"

if [ "$SKIP_BUILD" = "1" ]; then
  exec "$(dirname "$0")/serve-web.sh"
fi

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

masterdoc_web_free_build_memory "$ROOT"
masterdoc_web_stop_listener "$PORT"

API_HINT="$(grep -E '^masterdoc\.api\.baseUrl=' "$ROOT/local.properties" 2>/dev/null | cut -d= -f2- || echo 'http://api.masterdoc.pro/v1')"
echo "Web: http://127.0.0.1:${PORT}/"
echo "API: ${API_HINT} (local.properties masterdoc.api.baseUrl)"
echo "Только сервер (без сборки): MASTERDOC_WEB_SKIP_BUILD=1 $0"
echo "Остановка: Ctrl+C"
exec python3 -m http.server "$PORT" --bind 127.0.0.1 --directory "$PKG/kotlin"
