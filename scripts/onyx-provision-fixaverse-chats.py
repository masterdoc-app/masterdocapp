#!/usr/bin/env python3
"""
Provision 10 Fixaverse PDF chat personas in Onyx (one PDF at a time, wait for indexing).

Run on the Onyx server:
  source /etc/masterdoc/backend.env
  python3 onyx-provision-fixaverse-chats.py 2>&1 | tee /var/log/fixaverse-provision.log

Env: ONYX_BASE_URL (default http://127.0.0.1:3000/api), ONYX_PAT (required)
State: /tmp/fixaverse-provision-state.json (resume-safe)
"""
from __future__ import annotations

import json
import os
import subprocess
import sys
import time
import urllib.error
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from fixaverse_chat_system_prompt import FIXAVERSE_CHAT_SYSTEM_PROMPT

REFRESH_FREQ_SEC = 1_800_000_000  # ~57 years (matches existing docs connector)
PRUNE_FREQ_SEC = 864_000_000
PDF_DIR = Path("/tmp/fixaverse-pdf")
STATE_PATH = Path("/tmp/fixaverse-provision-state.json")
POLL_INTERVAL_SEC = 90
POLL_MAX_SEC = 3 * 60 * 60  # 3 h per PDF
SEARCH_TOOL_ID = 1
OOM_RETRY_WAIT_SEC = 300


@dataclass(frozen=True)
class ChatItem:
    persona_name: str
    base_name: str
    url: str
    filename: str
    description: str
    smoke_question: str


ITEMS: list[ChatItem] = [
    ChatItem(
        "Стол холодильный-chat",
        "Стол холодильный",
        "https://www.polair.com/upload/iblock/ec9/9pxxx9u2gb05cgc32o2x9rxss115xiy2.pdf",
        "polair-stol.pdf",
        "Стол холодильный — руководство по эксплуатации (PDF)",
        "Какие неисправности и способы устранения если нет электропитания?",
    ),
    ChatItem(
        "Насос 1ЦНСг-chat",
        "Насос 1ЦНСг",
        "https://www.gidromash-nasos.ru/upload/iblock/217/2173f4c413174405f94ff2fb71a67087.pdf",
        "livgidromash-1cnsg.pdf",
        "Насосы центробежные многоступенчатые секционные типа 1ЦНСг — РЭ PDF",
        "Насос не обеспечивает требуемых параметров — какие причины и способы устранения?",
    ),
    ChatItem(
        "Вентилятор ВЦ 14-46-chat",
        "Вентилятор ВЦ 14-46",
        "https://teplomash.ru/filereturn.php?datafile=/docroot/filesup/passport/passport_VZ14-46.pdf",
        "teplomash-vz14.pdf",
        "Вентиляторы центробежные ВЦ 14-46 — технический паспорт PDF",
        "Колесо вентилятора вращается в обратную сторону — что делать?",
    ),
    ChatItem(
        "Шкаф холодильный CRt-20-chat",
        "Шкаф холодильный CRt-20",
        "https://www.polair.com/upload/iblock/814/u25j9ckcnyfppzvznec9fcdymx6hs2hi/RE-apparat-shokovoy-zamorozki-CRt-20-Light-ot-28.08.2024.pdf",
        "polair-shkaf.pdf",
        "Шкаф холодильный, аппарат шоковой заморозки серии Light CRt-20 — РЭ PDF",
        "Перечень возможных неисправностей при повреждении шнура питания",
    ),
    ChatItem(
        "Компрессор RCM-chat",
        "Компрессор RCM",
        "https://ridan.ru/files/1799/1799720-141R8602_%D0%A0%D1%83%D0%BA%D0%BE%D0%B2%D0%BE%D0%B4%D1%81%D1%82%D0%B2%D0%BE_%D0%BF%D0%BE_%D1%8D%D0%BA%D1%81%D0%BF%D0%BB%D1%83%D0%B0%D1%82%D0%B0%D1%86%D0%B8%D0%B8.pdf",
        "ridan-rcm.pdf",
        "Компрессор герметичный спиральный типа RCM — руководство по эксплуатации PDF",
        "Компрессор не запускается — что проверить в первую очередь?",
    ),
    ChatItem(
        "Насос 1К-chat",
        "Насос 1К",
        "http://www.orelmash.ru/wp-content/uploads/pasp/k/rukovodstvo_nasos_1K_n49.899.00.000.pdf",
        "orelmash-1k.pdf",
        "Насосы центробежные консольные типа 1К — руководство по эксплуатации PDF",
        "Критические неисправности насоса — вибрация и скачки манометра",
    ),
    ChatItem(
        "Машина холодильная моноблочная-chat",
        "Машина холодильная моноблочная",
        "https://www.polair.com/upload/iblock/81f/myemgd3vg6z01xpcusbofwh6s5xbi1gq.pdf",
        "polair-split.pdf",
        "Машина холодильная моноблочная — руководство по эксплуатации PDF",
        "Холодильная машина не работает, не горит лампочка сеть — что проверить?",
    ),
    ChatItem(
        "Двигатель ВА 132–225-chat",
        "Двигатель ВА 132–225",
        "https://www.ruselprom.ru/upload/rukovodstvo-po-ekspluatatsii-vzryvozashchishchennykh-dvigateley-va-132_225_new.pdf",
        "ruselprom-va.pdf",
        "Двигатели асинхронные взрывозащищённые ВА 132–225 — РЭ PDF",
        "Возможные неисправности двигателя и методы их устранения",
    ),
    ChatItem(
        "ТРМ251-chat",
        "ТРМ251",
        "https://owen.ru/downloads/re_trm251.pdf",
        "owen-trm251.pdf",
        "Измеритель-регулятор программный ТРМ251 — руководство по эксплуатации PDF",
        "Неисправность датчика ТРМ251 — что проверить?",
    ),
    ChatItem(
        "ПЧВ3-chat",
        "ПЧВ3",
        "https://owen.ru/downloads/re_pchv3_m01.pdf",
        "owen-pchv3.txt",
        "Преобразователь частоты векторный ПЧВ3 — руководство по эксплуатации (TXT, pdftotext)",
        "Коды неисправностей ПЧВ3 и способы их устранения",
    ),
]


def log(msg: str) -> None:
    line = f"[{time.strftime('%Y-%m-%dT%H:%M:%S')}] {msg}"
    print(line, flush=True)


def load_state() -> dict[str, Any]:
    if STATE_PATH.exists():
        return json.loads(STATE_PATH.read_text(encoding="utf-8"))
    return {"completed": {}, "failed": {}}


def save_state(state: dict[str, Any]) -> None:
    STATE_PATH.write_text(json.dumps(state, ensure_ascii=False, indent=2), encoding="utf-8")


class OnyxClient:
    def __init__(self, base_url: str, pat: str) -> None:
        self.base = base_url.rstrip("/")
        self.pat = pat

    def request(
        self,
        method: str,
        path: str,
        body: dict | None = None,
        *,
        timeout: int = 120,
        raw_data: bytes | None = None,
        content_type: str = "application/json",
        headers_extra: dict[str, str] | None = None,
    ) -> tuple[int, str]:
        url = f"{self.base}{path}"
        headers = {"Authorization": f"Bearer {self.pat}", "Accept": "application/json"}
        if headers_extra:
            headers.update(headers_extra)
        data: bytes | None = None
        if raw_data is not None:
            data = raw_data
            headers["Content-Type"] = content_type
        elif body is not None:
            data = json.dumps(body).encode("utf-8")
            headers["Content-Type"] = "application/json"
        req = urllib.request.Request(url, data=data, method=method, headers=headers)
        try:
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return resp.status, resp.read().decode("utf-8", errors="replace")
        except urllib.error.HTTPError as e:
            return e.code, e.read().decode("utf-8", errors="replace")

    def request_json(self, method: str, path: str, body: dict | None = None, **kwargs: Any) -> Any:
        code, raw = self.request(method, path, body, **kwargs)
        if code not in (200, 201):
            raise RuntimeError(f"{method} {path} HTTP {code}: {raw[:800]}")
        if not raw.strip():
            return None
        return json.loads(raw)

    def upload_file(self, pdf_path: Path) -> tuple[str, str]:
        url = f"{self.base}/manage/admin/connector/file/upload"
        result = subprocess.run(
            [
                "curl",
                "-fsS",
                "--max-time",
                "300",
                "-H",
                f"Authorization: Bearer {self.pat}",
                "-F",
                f"files=@{pdf_path}",
                "-F",
                "unzip=false",
                url,
            ],
            capture_output=True,
            text=True,
            check=True,
        )
        data = json.loads(result.stdout)
        return data["file_paths"][0], data["file_names"][0]

    def list_personas(self) -> list[dict]:
        return self.request_json("GET", "/persona")

    def find_persona_by_name(self, name: str) -> dict | None:
        for p in self.list_personas():
            if p.get("name") == name:
                return p
        return None

    def create_connector(self, base_name: str, file_id: str, file_name: str) -> int:
        payload = {
            "name": base_name,
            "source": "file",
            "input_type": "load_state",
            "connector_specific_config": {
                "file_locations": [file_id],
                "file_names": [file_name],
            },
            "refresh_freq": REFRESH_FREQ_SEC,
            "prune_freq": PRUNE_FREQ_SEC,
            "access_type": "public",
            "groups": [],
        }
        resp = self.request_json("POST", "/manage/admin/connector-with-mock-credential", payload)
        cc_pair_id = resp.get("data")
        if not isinstance(cc_pair_id, int):
            raise RuntimeError(f"Unexpected connector response: {resp}")
        return cc_pair_id

    def create_document_set(self, base_name: str, description: str, cc_pair_id: int) -> int:
        payload = {
            "name": base_name,
            "description": description,
            "cc_pair_ids": [cc_pair_id],
            "is_public": True,
            "groups": [],
        }
        doc_set_id = self.request_json("POST", "/manage/admin/document-set", payload)
        if not isinstance(doc_set_id, int):
            raise RuntimeError(f"Unexpected document-set response: {doc_set_id}")
        return doc_set_id

    def create_persona(self, item: ChatItem, doc_set_id: int) -> int:
        payload = {
            "name": item.persona_name,
            "description": item.description,
            "document_set_ids": [doc_set_id],
            "is_public": False,
            "tool_ids": [SEARCH_TOOL_ID],
            "system_prompt": FIXAVERSE_CHAT_SYSTEM_PROMPT,
            "task_prompt": "",
            "datetime_aware": True,
            "replace_base_system_prompt": True,
            "users": [],
            "groups": [],
        }
        resp = self.request_json("POST", "/persona", payload)
        persona_id = resp.get("id")
        if not isinstance(persona_id, int):
            raise RuntimeError(f"Unexpected persona response: {resp}")
        return persona_id

    def get_document_set(self, doc_set_id: int) -> dict | None:
        sets = self.request_json("GET", "/manage/document-set")
        for ds in sets:
            if ds.get("id") == doc_set_id:
                return ds
        return None

    def indexing_status_for(self, connector_name: str) -> dict | None:
        payload = {"secondary_index": False, "name_filter": connector_name}
        groups = self.request_json("POST", "/manage/admin/connector/indexing-status", payload)
        for group in groups:
            for st in group.get("indexing_statuses", []):
                if st.get("name") == connector_name:
                    return st
        return None

    def trigger_reindex(self, connector_id: int, credential_id: int) -> None:
        payload = {
            "connector_id": connector_id,
            "credential_ids": [credential_id],
            "from_beginning": True,
        }
        self.request_json("POST", "/manage/admin/connector/run-once", payload)

    def resolve_connector_ids(self, cc_pair_id: int) -> tuple[int, int]:
        statuses = self.request_json("GET", "/manage/admin/connector/status")
        for item in statuses:
            if item.get("cc_pair_id") == cc_pair_id:
                return item["connector"]["id"], item["credential"]["id"]
        raise RuntimeError(f"Cannot resolve connector/credential for cc_pair {cc_pair_id}")

    def wait_indexed(self, connector_name: str, doc_set_id: int) -> None:
        deadline = time.time() + POLL_MAX_SEC
        attempt = 0
        while time.time() < deadline:
            attempt += 1
            st = self.indexing_status_for(connector_name)
            ds = self.get_document_set(doc_set_id)
            ds_ok = bool(ds and ds.get("is_up_to_date"))
            if st:
                log(
                    f"  poll #{attempt} {connector_name}: in_progress={st.get('in_progress')} "
                    f"last={st.get('last_finished_status')} docs={st.get('docs_indexed')} "
                    f"doc_set_up_to_date={ds_ok}"
                )
                finished = (st.get("last_finished_status") or "").lower()
                if (
                    not st.get("in_progress")
                    and finished == "success"
                    and (st.get("docs_indexed") or 0) >= 1
                    and ds_ok
                ):
                    return
                if not st.get("in_progress") and finished == "failed":
                    err = st.get("last_status", "")
                    raise RuntimeError(f"Indexing failed for {connector_name}: {err}")
            else:
                log(f"  poll #{attempt} {connector_name}: status not found yet")
            time.sleep(POLL_INTERVAL_SEC)
        raise TimeoutError(f"Indexing timeout for {connector_name} after {POLL_MAX_SEC}s")

    def smoke_test(self, persona_id: int, question: str) -> str:
        session = self.request_json(
            "POST",
            "/chat/create-chat-session",
            {"persona_id": persona_id},
        )
        session_id = session["chat_session_id"]
        payload = {
            "message": question,
            "chat_session_id": session_id,
            "stream": False,
            "forced_tool_id": SEARCH_TOOL_ID,
        }
        code, raw = self.request("POST", "/chat/send-chat-message", payload, timeout=600)
        if code != 200:
            raise RuntimeError(f"smoke test HTTP {code}: {raw[:500]}")
        # Response may be JSON or SSE-ish; extract answer text
        try:
            data = json.loads(raw)
            if isinstance(data, dict):
                return str(data.get("answer") or data.get("message") or raw[:300])
        except json.JSONDecodeError:
            pass
        return raw[:500]


def download_pdf(url: str, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    log(f"Downloading {url} -> {dest}")
    subprocess.run(
        ["curl", "-fsSL", "--max-time", "600", "-o", str(dest), url],
        check=True,
    )
    size = dest.stat().st_size
    if size < 10_000:
        raise RuntimeError(f"Download too small ({size} bytes): {url}")
    log(f"Downloaded {size // 1024} KB")


def check_memory() -> None:
    try:
        out = subprocess.check_output(["free", "-h"], text=True)
        log(out.strip().replace("\n", " | "))
    except Exception:
        pass


def provision_item(client: OnyxClient, item: ChatItem, state: dict[str, Any]) -> None:
    key = item.persona_name
    if state.get("completed", {}).get(key):
        log(f"SKIP (already completed): {key}")
        return

    existing = client.find_persona_by_name(item.persona_name)
    if existing:
        log(f"Persona already exists: {item.persona_name} id={existing['id']} — marking completed")
        state.setdefault("completed", {})[key] = {"persona_id": existing["id"], "skipped": True}
        save_state(state)
        return

    log(f"=== START {item.persona_name} ===")
    check_memory()

    source_path = PDF_DIR / item.filename
    if item.filename.endswith(".txt"):
        pdf_path = source_path.with_suffix(".pdf")
        if not pdf_path.exists():
            download_pdf(item.url, pdf_path)
        if not source_path.exists():
            log(f"Converting PDF to text: {pdf_path.name} -> {source_path.name}")
            subprocess.run(["pdftotext", "-enc", "UTF-8", str(pdf_path), str(source_path)], check=True)
        upload_path = source_path
    else:
        upload_path = source_path
        if not upload_path.exists():
            download_pdf(item.url, upload_path)

    file_id, uploaded_name = client.upload_file(upload_path)
    log(f"Uploaded file_id={file_id} name={uploaded_name}")

    cc_pair_id = client.create_connector(item.base_name, file_id, uploaded_name)
    log(f"Created connector cc_pair_id={cc_pair_id}")

    doc_set_id = client.create_document_set(item.base_name, item.description, cc_pair_id)
    log(f"Created document_set id={doc_set_id}")

    persona_id = client.create_persona(item, doc_set_id)
    log(f"Created persona id={persona_id}")

    try:
        client.wait_indexed(item.base_name, doc_set_id)
    except RuntimeError as e:
        if "failed" in str(e).lower() or "exit" in str(e).lower():
            log(f"Indexing failed, waiting {OOM_RETRY_WAIT_SEC}s before retry...")
            time.sleep(OOM_RETRY_WAIT_SEC)
            check_memory()
            conn_id, cred_id = client.resolve_connector_ids(cc_pair_id)
            client.trigger_reindex(conn_id, cred_id)
            client.wait_indexed(item.base_name, doc_set_id)
        else:
            raise

    answer = client.smoke_test(persona_id, item.smoke_question)
    log(f"Smoke test answer snippet: {answer[:200].replace(chr(10), ' ')}")

    state.setdefault("completed", {})[key] = {
        "persona_id": persona_id,
        "doc_set_id": doc_set_id,
        "cc_pair_id": cc_pair_id,
        "smoke_snippet": answer[:300],
    }
    save_state(state)
    log(f"=== DONE {item.persona_name} ===")


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


def main() -> int:
    load_env_file(Path("/etc/masterdoc/backend.env"))
    base = os.environ.get("ONYX_BASE_URL", "http://127.0.0.1:3000/api")
    pat = os.environ.get("ONYX_PAT", "")
    if not pat:
        log("ERROR: set ONYX_PAT")
        return 1

    client = OnyxClient(base, pat)
    state = load_state()
    log(f"Starting Fixaverse provision ({len(ITEMS)} items), state={STATE_PATH}")

    for item in ITEMS:
        try:
            provision_item(client, item, state)
        except Exception as e:
            log(f"FATAL on {item.persona_name}: {e}")
            state.setdefault("failed", {})[item.persona_name] = str(e)
            save_state(state)
            return 2

    log("All 10 chat personas provisioned successfully.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
