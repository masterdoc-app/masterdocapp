#!/usr/bin/env python3
"""Send test troubleshooting questions to all Fixaverse -chat personas."""
from __future__ import annotations

import json
import os
import re
import sys
import urllib.request
from pathlib import Path

SEARCH_TOOL = 1

TESTS = [
    (5, "Стол холодильный-chat", "Какие неисправности и способы устранения если нет электропитания?"),
    (6, "Насос 1ЦНСг-chat", "Насос не обеспечивает требуемых параметров — какие причины и способы устранения?"),
    (7, "Вентилятор ВЦ 14-46-chat", "Колесо вентилятора вращается в обратную сторону — что делать?"),
    (8, "Шкаф холодильный CRt-20-chat", "Что делать при повреждении шнура питания?"),
    (9, "Компрессор RCM-chat", "Компрессор не запускается — что проверить в первую очередь?"),
    (10, "Насос 1К-chat", "Насос гудит — что проверить по таблице неисправностей?"),
    (11, "Машина холодильная моноблочная-chat", "Холодильная машина не работает, не горит лампочка «сеть» — что проверить?"),
    (12, "Двигатель ВА 132–225-chat", "Двигатель при пуске не разворачивается и гудит — что делать?"),
    (13, "ТРМ251-chat", "Неисправность датчика ТРМ251 — что показывает прибор и что проверить?"),
    (14, "ПЧВ3-chat", "Код E.SC1 на экране ПЧВ3 — что означает и как устранить по приложению А?"),
]


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


def api(base: str, pat: str, method: str, path: str, body: dict | None = None, timeout: int = 600) -> str:
    url = base.rstrip("/") + path
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(
        url,
        data=data,
        method=method,
        headers={
            "Authorization": f"Bearer {pat}",
            "Content-Type": "application/json",
            "Accept": "application/json",
        },
    )
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return resp.read().decode("utf-8", errors="replace")


def parse_answer(raw: str) -> str:
    try:
        data = json.loads(raw)
        if isinstance(data, dict):
            return str(data.get("answer") or data.get("message") or raw[:800])
    except json.JSONDecodeError:
        pass
    parts: list[str] = []
    for line in raw.splitlines():
        if line.startswith("data:"):
            chunk = line[5:].strip()
            if chunk and chunk != "[DONE]":
                try:
                    obj = json.loads(chunk)
                    if obj.get("answer"):
                        parts.append(str(obj["answer"]))
                except json.JSONDecodeError:
                    pass
    return "".join(parts) if parts else raw[:800]


def is_good_answer(answer: str) -> bool:
    low = answer.lower()
    if not answer.strip():
        return False
    if "нет подходящего" in low:
        return False
    if "атлант" in low:
        return False
    if "бытов" in low and "техник" in low:
        return False
    if len(answer) < 60:
        return False
    return True


def main() -> int:
    load_env_file(Path("/etc/masterdoc/backend.env"))
    base = os.environ.get("ONYX_BASE_URL", "http://127.0.0.1:3000/api")
    pat = os.environ.get("ONYX_PAT", "")
    if not pat:
        print("ONYX_PAT required", file=sys.stderr)
        return 1

    results: list[dict] = []
    sep = "=" * 60
    dash = "-" * 60

    for persona_id, name, question in TESTS:
        print(f"\n{sep}\nCHAT: {name} (id={persona_id})\nQ: {question}\n{dash}")
        try:
            session = json.loads(api(base, pat, "POST", "/chat/create-chat-session", {"persona_id": persona_id}))
            session_id = session["chat_session_id"]
            raw = api(
                base,
                pat,
                "POST",
                "/chat/send-chat-message",
                {
                    "message": question,
                    "chat_session_id": session_id,
                    "stream": False,
                    "forced_tool_id": SEARCH_TOOL,
                },
            )
            answer = parse_answer(raw).strip()
            short = re.sub(r"\s+", " ", answer)[:1200]
            ok = is_good_answer(answer)
            print(f"A: {short}")
            print(f"STATUS: {'OK' if ok else 'WEAK'}")
            results.append({"persona": name, "id": persona_id, "ok": ok, "answer_len": len(answer)})
        except Exception as exc:
            print(f"ERROR: {exc}")
            results.append({"persona": name, "id": persona_id, "ok": False, "error": str(exc)})

    ok_count = sum(1 for r in results if r.get("ok"))
    print(f"\n{sep}\nSUMMARY: {ok_count}/{len(results)} OK")
    for row in results:
        mark = "OK" if row.get("ok") else "FAIL"
        print(f"  {mark} {row['persona']}")
    return 0 if ok_count == len(results) else 2


if __name__ == "__main__":
    raise SystemExit(main())
