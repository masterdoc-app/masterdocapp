#!/usr/bin/env bash
# Shared helpers for run-web.sh / serve-web.sh

masterdoc_web_stop_listener() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    local pid args
    for pid in $(lsof -ti ":${port}" 2>/dev/null || true); do
      args="$(ps -p "$pid" -o args= 2>/dev/null || true)"
      case "$args" in
        *"http.server ${port}"*|*"http.server ${port} "*|*"http.server"*"${port}"*)
          kill "$pid" 2>/dev/null || true
          ;;
      esac
    done
    sleep 0.5
    return 0
  fi
  if command -v fuser >/dev/null 2>&1; then
    # Fallback: only when lsof is unavailable (fuser -k kills every process on the port).
    fuser -k "${port}/tcp" >/dev/null 2>&1 || true
    sleep 0.5
  fi
}

masterdoc_web_free_build_memory() {
  local root="$1"
  if [ -x "$root/gradlew" ]; then
    (cd "$root" && ./gradlew --stop >/dev/null 2>&1) || true
  fi
}
