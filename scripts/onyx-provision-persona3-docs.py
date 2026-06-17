#!/usr/bin/env python3
"""
Replace Atlant docs on persona id=3 with all 10 Fixaverse equipment PDFs (document set id=3).

Reuses existing FILE connector cc_pairs (already indexed for -chat personas).
Adds them to document set 3 one at a time; waits for is_up_to_date after each.

Run on Onyx server:
  python3 onyx-provision-persona3-docs.py 2>&1 | tee /var/log/fixaverse-persona3.log
"""
from __future__ import annotations

import json
import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any

PERSONA_ID = 3
DOCUMENT_SET_ID = 3
POLL_INTERVAL_SEC = 60
POLL_MAX_SEC = 2 * 60 * 60
STATE_PATH = Path("/tmp/fixaverse-persona3-state.json")

# Order: smallest PDF first (same as table 10)
EQUIPMENT_CC_PAIRS: list[tuple[int, str]] = [
    (7, "Стол холодильный"),
    (8, "Насос 1ЦНСг"),
    (9, "Вентилятор ВЦ 14-46"),
    (10, "Шкаф холодильный CRt-20"),
    (11, "Компрессор RCM"),
    (12, "Насос 1К"),
    (13, "Машина холодильная моноблочная"),
    (14, "Двигатель ВА 132–225"),
    (15, "ТРМ251"),
    (16, "ПЧВ3"),
]

DOC_SET_NAME = "Fixaverse-оборудование-доки"
PERSONA_NAME = "Fixaverse-оборудование-доки"
PERSONA_DESCRIPTION = (
    "Сводная база РЭ на русском: 10 единиц промышленного оборудования Fixaverse "
    "(холод, насосы, вентиляция, двигатели, автоматика ОВЕН)."
)
SYSTEM_PROMPT = (
    "Тебе нужно определить один из чатов по фото оборудования и вернуть только "
    "название одного чата из списка доступных ассистентов."
)


def load_env_file(path: Path) -> None:
    if not path.exists():
        return
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        key = key.strip()
        value = value.strip().strip('"').strip("'")
        if key and key not in os.environ:
            os.environ[key] = value


def log(msg: str) -> None:
    print(f"[{time.strftime('%Y-%m-%dT%H:%M:%S')}] {msg}", flush=True)


class Client:
    def __init__(self, base: str, pat: str) -> None:
        self.base = base.rstrip("/")
        self.pat = pat

    def request(self, method: str, path: str, body: dict | None = None, timeout: int = 120) -> tuple[int, str]:
        url = self.base + path
        data = json.dumps(body).encode() if body is not None else None
        req = urllib.request.Request(
            url,
            data=data,
            method=method,
            headers={
                "Authorization": f"Bearer {self.pat}",
                "Content-Type": "application/json",
                "Accept": "application/json",
            },
        )
        try:
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return resp.status, resp.read().decode("utf-8", errors="replace")
        except urllib.error.HTTPError as e:
            return e.code, e.read().decode("utf-8", errors="replace")

    def json(self, method: str, path: str, body: dict | None = None, timeout: int = 120) -> Any:
        code, raw = self.request(method, path, body, timeout=timeout)
        if code not in (200, 201):
            raise RuntimeError(f"{method} {path} HTTP {code}: {raw[:800]}")
        return json.loads(raw) if raw.strip() else None

    def get_document_set(self, doc_set_id: int) -> dict:
        for ds in self.json("GET", "/manage/document-set"):
            if ds["id"] == doc_set_id:
                return ds
        raise RuntimeError(f"document set {doc_set_id} not found")

    def patch_document_set(self, cc_pair_ids: list[int]) -> None:
        payload = {
            "id": DOCUMENT_SET_ID,
            "name": DOC_SET_NAME,
            "description": PERSONA_DESCRIPTION,
            "cc_pair_ids": cc_pair_ids,
            "is_public": True,
            "users": [],
            "groups": [],
            "federated_connectors": [],
        }
        self.json("PATCH", "/manage/admin/document-set", payload, timeout=180)

    def wait_document_set_ready(self, expected_pairs: int) -> None:
        deadline = time.time() + POLL_MAX_SEC
        attempt = 0
        while time.time() < deadline:
            attempt += 1
            ds = self.get_document_set(DOCUMENT_SET_ID)
            pairs = ds.get("cc_pair_summaries") or []
            up = ds.get("is_up_to_date")
            log(
                f"  doc-set poll #{attempt}: pairs={len(pairs)}/{expected_pairs} "
                f"is_up_to_date={up} names={[p['name'] for p in pairs]}"
            )
            if up and len(pairs) == expected_pairs:
                return
            time.sleep(POLL_INTERVAL_SEC)
        raise TimeoutError(f"document set {DOCUMENT_SET_ID} not ready after {POLL_MAX_SEC}s")

    def patch_persona(self) -> None:
        payload = {
            "name": PERSONA_NAME,
            "description": PERSONA_DESCRIPTION,
            "document_set_ids": [DOCUMENT_SET_ID],
            "is_public": False,
            "default_model_configuration_id": 3,
            "tool_ids": [1],
            "system_prompt": SYSTEM_PROMPT,
            "task_prompt": "",
            "datetime_aware": True,
            "users": [],
            "groups": [],
            "is_featured": False,
        }
        self.json("PATCH", f"/persona/{PERSONA_ID}", payload, timeout=120)


def load_state() -> dict[str, Any]:
    if STATE_PATH.exists():
        return json.loads(STATE_PATH.read_text(encoding="utf-8"))
    return {"added_count": 0}


def save_state(state: dict[str, Any]) -> None:
    STATE_PATH.write_text(json.dumps(state, ensure_ascii=False, indent=2), encoding="utf-8")


def main() -> int:
    load_env_file(Path("/etc/masterdoc/backend.env"))
    base = os.environ.get("ONYX_BASE_URL", "http://127.0.0.1:3000/api")
    pat = os.environ.get("ONYX_PAT", "")
    if not pat:
        log("ONYX_PAT required")
        return 1

    client = Client(base, pat)
    state = load_state()
    start_from = int(state.get("added_count", 0))

    log(f"Persona {PERSONA_ID} / document set {DOCUMENT_SET_ID}: add {len(EQUIPMENT_CC_PAIRS)} PDF connectors")
    if start_from:
        log(f"Resuming from {start_from} connectors already added")

    accumulated: list[int] = []
    for idx, (cc_pair_id, name) in enumerate(EQUIPMENT_CC_PAIRS, start=1):
        if idx <= start_from:
            accumulated.append(cc_pair_id)
            continue
        accumulated.append(cc_pair_id)
        log(f"=== Add {idx}/10: {name} (cc_pair {cc_pair_id}) ===")
        client.patch_document_set(accumulated)
        client.wait_document_set_ready(len(accumulated))
        state["added_count"] = idx
        state["cc_pair_ids"] = accumulated
        save_state(state)
        log(f"=== Ready {idx}/10: {name} ===")

    log("Updating persona 3 (name, document sets, prompt)...")
    client.patch_persona()

    ds = client.get_document_set(DOCUMENT_SET_ID)
    log(f"DONE persona id={PERSONA_ID} name={PERSONA_NAME} doc_pairs={len(ds.get('cc_pair_summaries', []))}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
