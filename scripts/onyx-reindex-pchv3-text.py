#!/usr/bin/env python3
"""Re-index ПЧВ3: replace broken 41MB PDF with pdftotext export for Onyx file connector."""
from __future__ import annotations

import json
import os
import subprocess
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

PDF_PATH = Path("/tmp/fixaverse-pdf/owen-pchv3.pdf")
TXT_PATH = Path("/tmp/fixaverse-pdf/owen-pchv3.txt")
APPENDIX_TXT = Path("/tmp/fixaverse-pdf/owen-pchv3-prilozhenie-a.txt")
CC_PAIR_ID = 16
CONNECTOR_ID = 17
CREDENTIAL_ID = 17
POLL_INTERVAL_SEC = 30
POLL_MAX_SEC = 30 * 60


def load_env() -> None:
    for line in Path("/etc/masterdoc/backend.env").read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, _, v = line.partition("=")
            os.environ.setdefault(k.strip(), v.strip().strip('"').strip("'"))


def api(base: str, pat: str, method: str, path: str, body: dict | None = None, timeout: int = 180) -> tuple[int, str]:
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
            return resp.status, resp.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()


def upload_file(base: str, pat: str, path: Path) -> tuple[str, str]:
    result = subprocess.run(
        [
            "curl",
            "-fsS",
            "--max-time",
            "300",
            "-H",
            f"Authorization: Bearer {pat}",
            "-F",
            f"files=@{path}",
            "-F",
            "unzip=false",
            f"{base.rstrip('/')}/manage/admin/connector/file/upload",
        ],
        capture_output=True,
        text=True,
        check=True,
    )
    data = json.loads(result.stdout)
    return data["file_paths"][0], data["file_names"][0]


def extract_text() -> None:
    if not PDF_PATH.exists():
        raise FileNotFoundError(PDF_PATH)
    subprocess.run(["pdftotext", "-enc", "UTF-8", str(PDF_PATH), str(TXT_PATH)], check=True)
    subprocess.run(
        ["pdftotext", "-enc", "UTF-8", "-f", "214", "-l", "249", str(PDF_PATH), str(APPENDIX_TXT)],
        check=True,
    )
    full_len = TXT_PATH.stat().st_size
    app_len = APPENDIX_TXT.stat().st_size
    print(f"Extracted {TXT_PATH.name}: {full_len} bytes")
    print(f"Extracted {APPENDIX_TXT.name}: {app_len} bytes")


def get_connector_status(base: str, pat: str) -> dict:
    code, raw = api(base, pat, "GET", "/manage/admin/connector/status")
    if code != 200:
        raise RuntimeError(f"connector/status HTTP {code}: {raw[:300]}")
    for item in json.loads(raw):
        if item.get("cc_pair_id") == CC_PAIR_ID:
            return item
    raise RuntimeError(f"cc_pair {CC_PAIR_ID} not found")


def patch_connector(base: str, pat: str, status_item: dict, file_ids: list[str], file_names: list[str]) -> None:
    connector = status_item["connector"]
    payload = {
        "name": connector["name"],
        "source": connector["source"],
        "input_type": connector["input_type"],
        "connector_specific_config": {
            "file_locations": file_ids,
            "file_names": file_names,
        },
        "refresh_freq": connector.get("refresh_freq"),
        "prune_freq": connector.get("prune_freq"),
        "indexing_start": connector.get("indexing_start"),
        "access_type": status_item["access_type"],
        "groups": status_item.get("groups") or [],
    }
    code, raw = api(base, pat, "PATCH", f"/manage/admin/connector/{CONNECTOR_ID}", payload)
    if code != 200:
        raise RuntimeError(f"PATCH connector HTTP {code}: {raw[:500]}")


def trigger_reindex(base: str, pat: str) -> None:
    payload = {
        "connector_id": CONNECTOR_ID,
        "credential_ids": [CREDENTIAL_ID],
        "from_beginning": True,
    }
    code, raw = api(base, pat, "POST", "/manage/admin/connector/run-once", payload)
    if code != 200:
        raise RuntimeError(f"run-once HTTP {code}: {raw[:500]}")


def wait_indexed(base: str, pat: str) -> int:
    deadline = time.time() + POLL_MAX_SEC
    saw_in_progress = False
    while time.time() < deadline:
        code, raw = api(base, pat, "GET", "/manage/admin/connector/indexing-status")
        if code != 200:
            code, raw = api(
                base,
                pat,
                "POST",
                "/manage/admin/connector/indexing-status",
                {"secondary_index": False, "name_filter": "ПЧВ3"},
            )
        if code != 200:
            raise RuntimeError(f"indexing-status HTTP {code}")
        groups = json.loads(raw) if raw.strip().startswith("[") else []
        if isinstance(groups, dict):
            groups = [groups]
        for group in groups:
            for st in group.get("indexing_statuses", []):
                if st.get("cc_pair_id") != CC_PAIR_ID:
                    continue
                in_prog = bool(st.get("in_progress"))
                print(
                    f"  status: in_progress={in_prog} last={st.get('last_status')} "
                    f"docs={st.get('docs_indexed')}"
                )
                if in_prog:
                    saw_in_progress = True
                elif saw_in_progress and st.get("last_status") == "success":
                    return int(st.get("docs_indexed") or 0)
        time.sleep(POLL_INTERVAL_SEC)
    raise TimeoutError("indexing did not finish in time")


def chunk_count() -> int:
    cmd = [
        "docker",
        "exec",
        "onyx-relational_db-1",
        "psql",
        "-U",
        "postgres",
        "-d",
        "postgres",
        "-t",
        "-A",
        "-c",
        "SELECT COALESCE(SUM(chunk_count),0) FROM document WHERE semantic_id IN ('owen-pchv3.txt','owen-pchv3-prilozhenie-a.txt');",
    ]
    out = subprocess.run(cmd, capture_output=True, text=True, check=True).stdout.strip()
    return int(out) if out.isdigit() else 0


def test_chat(base: str, pat: str) -> str:
    code, raw = api(base, pat, "POST", "/chat/create-chat-session", {"persona_id": 14})
    sid = json.loads(raw)["chat_session_id"]
    code, raw = api(
        base,
        pat,
        "POST",
        "/chat/send-chat-message",
        {
            "message": "Код E.SC1 на экране ПЧВ3 — что означает и как устранить по приложению А?",
            "chat_session_id": sid,
            "stream": False,
            "forced_tool_id": 1,
        },
        timeout=600,
    )
    if code != 200:
        return f"HTTP {code}: {raw[:300]}"
    return json.loads(raw).get("answer") or ""


def main() -> int:
    load_env()
    base = os.environ.get("ONYX_BASE_URL", "http://127.0.0.1:3000/api")
    pat = os.environ.get("ONYX_PAT", "")
    if not pat:
        print("ONYX_PAT required", file=sys.stderr)
        return 1

    print("=== 1. Extract text from PDF ===")
    extract_text()

    print("=== 2. Upload text files ===")
    full_id, full_name = upload_file(base, pat, TXT_PATH)
    app_id, app_name = upload_file(base, pat, APPENDIX_TXT)
    print(f"  uploaded: {full_name}, {app_name}")

    print("=== 3. Update connector (replace PDF with TXT) ===")
    status = get_connector_status(base, pat)
    patch_connector(base, pat, status, [full_id, app_id], [full_name, app_name])

    print("=== 4. Reindex from beginning ===")
    trigger_reindex(base, pat)

    print("=== 5. Wait for indexing ===")
    docs = wait_indexed(base, pat)
    print(f"  docs_indexed={docs}")

    time.sleep(5)
    chunks = chunk_count()
    print(f"  chunk_count={chunks}")

    print("=== 6. Test chat persona 14 ===")
    answer = test_chat(base, pat)
    print("A:", answer[:1200])
    ok = "E.SC1" in answer or "короткое замыкание" in answer.lower()
    ok = ok and "нет подходящего" not in answer.lower()
    print("RESULT:", "OK" if ok else "WEAK")
    return 0 if ok else 2


if __name__ == "__main__":
    raise SystemExit(main())
