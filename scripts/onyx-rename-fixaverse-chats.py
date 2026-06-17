#!/usr/bin/env python3
"""Rename Fixaverse chat personas, document sets, and connectors to Cyrillic manual titles."""
from __future__ import annotations

import json
import os
import sys
import urllib.error
import urllib.request
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class RenameItem:
    persona_id: int
    doc_set_id: int
    cc_pair_id: int
    connector_id: int
    base_name: str
    description: str

    @property
    def chat_name(self) -> str:
        return f"{self.base_name}-chat"


RENAMES: list[RenameItem] = [
    RenameItem(5, 5, 7, 8, "Стол холодильный", "Стол холодильный — руководство по эксплуатации (PDF)"),
    RenameItem(6, 6, 8, 9, "Насос 1ЦНСг", "Насосы центробежные многоступенчатые секционные типа 1ЦНСг — РЭ PDF"),
    RenameItem(7, 7, 9, 10, "Вентилятор ВЦ 14-46", "Вентиляторы центробежные ВЦ 14-46 — технический паспорт PDF"),
    RenameItem(8, 8, 10, 11, "Шкаф холодильный CRt-20", "Шкаф холодильный, аппарат шоковой заморозки серии Light CRt-20 — РЭ PDF"),
    RenameItem(9, 9, 11, 12, "Компрессор RCM", "Компрессор герметичный спиральный типа RCM — руководство по эксплуатации PDF"),
    RenameItem(10, 10, 12, 13, "Насос 1К", "Насосы центробежные консольные типа 1К — руководство по эксплуатации PDF"),
    RenameItem(11, 11, 13, 14, "Машина холодильная моноблочная", "Машина холодильная моноблочная — руководство по эксплуатации PDF"),
    RenameItem(12, 12, 14, 15, "Двигатель ВА 132–225", "Двигатели асинхронные взрывозащищённые ВА 132–225 — РЭ PDF"),
    RenameItem(13, 13, 15, 16, "ТРМ251", "Измеритель-регулятор программный ТРМ251 — руководство по эксплуатации PDF"),
    RenameItem(14, 14, 16, 17, "ПЧВ3", "Преобразователь частоты векторный ПЧВ3 — руководство по эксплуатации PDF"),
]


def load_env() -> None:
    for line in Path("/etc/masterdoc/backend.env").read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, _, v = line.partition("=")
            os.environ.setdefault(k.strip(), v.strip().strip('"').strip("'"))


def api(base: str, pat: str, method: str, path: str, body: dict | None = None, timeout: int = 120) -> tuple[int, str]:
    url = base.rstrip("/") + path
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(
        url,
        data=data,
        method=method,
        headers={"Authorization": f"Bearer {pat}", "Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, resp.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()


def patch_persona(base: str, pat: str, item: RenameItem) -> None:
    code, raw = api(base, pat, "GET", f"/persona/{item.persona_id}")
    if code != 200:
        raise RuntimeError(f"GET persona {item.persona_id}: HTTP {code} {raw[:300]}")
    p = json.loads(raw)
    payload = {
        "name": item.chat_name,
        "description": item.description,
        "document_set_ids": [ds["id"] for ds in p.get("document_sets", [])],
        "is_public": p.get("is_public", False),
        "tool_ids": [t["id"] for t in p.get("tools", [])],
        "system_prompt": p.get("system_prompt") or "",
        "task_prompt": p.get("task_prompt") or "",
        "datetime_aware": p.get("datetime_aware", True),
        "users": [],
        "groups": [],
        "is_featured": p.get("is_featured", False),
    }
    if p.get("default_model_configuration_id") is not None:
        payload["default_model_configuration_id"] = p["default_model_configuration_id"]
    code, raw = api(base, pat, "PATCH", f"/persona/{item.persona_id}", payload)
    if code != 200:
        raise RuntimeError(f"PATCH persona {item.persona_id}: HTTP {code} {raw[:300]}")


def patch_document_set(base: str, pat: str, item: RenameItem) -> None:
    code, raw = api(base, pat, "GET", "/manage/document-set")
    if code != 200:
        raise RuntimeError(f"GET document-set: HTTP {code}")
    ds = next(d for d in json.loads(raw) if d["id"] == item.doc_set_id)
    payload = {
        "id": item.doc_set_id,
        "name": item.base_name,
        "description": item.description,
        "cc_pair_ids": [p["id"] for p in ds.get("cc_pair_summaries", [])],
        "is_public": ds.get("is_public", True),
        "users": ds.get("users") or [],
        "groups": ds.get("groups") or [],
        "federated_connectors": ds.get("federated_connector_summaries") or [],
    }
    code, raw = api(base, pat, "PATCH", "/manage/admin/document-set", payload)
    if code != 200:
        raise RuntimeError(f"PATCH document-set {item.doc_set_id}: HTTP {code} {raw[:300]}")


def patch_connector(base: str, pat: str, item: RenameItem, status_item: dict) -> None:
    connector = status_item["connector"]
    payload = {
        "name": item.base_name,
        "source": connector["source"],
        "input_type": connector["input_type"],
        "connector_specific_config": connector["connector_specific_config"],
        "refresh_freq": connector.get("refresh_freq"),
        "prune_freq": connector.get("prune_freq"),
        "indexing_start": connector.get("indexing_start"),
        "access_type": status_item["access_type"],
        "groups": status_item.get("groups") or [],
    }
    code, raw = api(base, pat, "PATCH", f"/manage/admin/connector/{item.connector_id}", payload)
    if code != 200:
        raise RuntimeError(f"PATCH connector {item.connector_id}: HTTP {code} {raw[:300]}")


def main() -> int:
    load_env()
    base = os.environ.get("ONYX_BASE_URL", "http://127.0.0.1:3000/api")
    pat = os.environ.get("ONYX_PAT", "")
    if not pat:
        print("ONYX_PAT required", file=sys.stderr)
        return 1

    code, raw = api(base, pat, "GET", "/manage/admin/connector/status")
    if code != 200:
        print(f"connector/status failed: {code}", file=sys.stderr)
        return 1
    by_cc = {x["cc_pair_id"]: x for x in json.loads(raw)}

    for item in RENAMES:
        print(f"=== {item.chat_name} (persona {item.persona_id}) ===")
        patch_connector(base, pat, item, by_cc[item.cc_pair_id])
        print(f"  connector {item.connector_id} -> {item.base_name}")
        patch_document_set(base, pat, item)
        print(f"  document set {item.doc_set_id} -> {item.base_name}")
        patch_persona(base, pat, item)
        print(f"  persona {item.persona_id} -> {item.chat_name}")

    print("DONE")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
