#!/usr/bin/env python3
"""Apply Fixaverse system prompt to chat personas (ids 5–14)."""
from __future__ import annotations

import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

# Allow import when run from /root on server
sys.path.insert(0, str(Path(__file__).resolve().parent))
from fixaverse_chat_system_prompt import FIXAVERSE_CHAT_SYSTEM_PROMPT  # noqa: E402

PERSONA_IDS = list(range(5, 15))


def load_env() -> None:
    for line in Path("/etc/masterdoc/backend.env").read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, _, v = line.partition("=")
            os.environ.setdefault(k.strip(), v.strip().strip('"').strip("'"))


def api(base: str, pat: str, method: str, path: str, body: dict | None = None, timeout: int = 120) -> dict:
    url = base.rstrip("/") + path
    data = json.dumps(body).encode() if body else None
    req = urllib.request.Request(
        url,
        data=data,
        method=method,
        headers={"Authorization": f"Bearer {pat}", "Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            raw = resp.read().decode()
            return json.loads(raw) if raw.strip() else {}
    except urllib.error.HTTPError as e:
        raise RuntimeError(f"{method} {path} HTTP {e.code}: {e.read().decode()[:500]}") from e


def patch_persona(base: str, pat: str, persona_id: int) -> str:
    p = api(base, pat, "GET", f"/persona/{persona_id}")
    payload = {
        "name": p["name"],
        "description": p.get("description") or "",
        "document_set_ids": [ds["id"] for ds in p.get("document_sets", [])],
        "is_public": p.get("is_public", False),
        "tool_ids": [t["id"] for t in p.get("tools", [])],
        "system_prompt": FIXAVERSE_CHAT_SYSTEM_PROMPT,
        "task_prompt": p.get("task_prompt") or "",
        "datetime_aware": p.get("datetime_aware", True),
        "replace_base_system_prompt": True,
        "users": [],
        "groups": [],
        "is_featured": p.get("is_featured", False),
    }
    if p.get("default_model_configuration_id") is not None:
        payload["default_model_configuration_id"] = p["default_model_configuration_id"]
    api(base, pat, "PATCH", f"/persona/{persona_id}", payload)
    return p["name"]


def main() -> int:
    load_env()
    base = os.environ.get("ONYX_BASE_URL", "http://127.0.0.1:3000/api")
    pat = os.environ.get("ONYX_PAT", "")
    if not pat:
        print("ONYX_PAT required", file=sys.stderr)
        return 1

    for pid in PERSONA_IDS:
        name = patch_persona(base, pat, pid)
        print(f"OK persona {pid}: {name}")

    print(f"DONE: {len(PERSONA_IDS)} personas updated")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
