#!/usr/bin/env python3
"""
Set maximum refresh/prune intervals on all Onyx connectors (admin API).
Reads onyx.baseUrl and onyx.pat from ../local.properties (repo root).
"""
from __future__ import annotations

import json
import re
import sys
import urllib.error
import urllib.request
from pathlib import Path

# 30 days — slowest practical schedule for a small VPS
REFRESH_FREQ_SEC = 30 * 24 * 60 * 60
PRUNE_FREQ_SEC = 30 * 24 * 60 * 60


def load_local_properties(root: Path) -> dict[str, str]:
    props: dict[str, str] = {}
    path = root / "local.properties"
    if not path.exists():
        return props
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        props[key.strip()] = value.strip()
    return props


def api_request(
    base: str,
    pat: str,
    method: str,
    path: str,
    body: dict | None = None,
    timeout: int = 60,
) -> tuple[int, str]:
    url = f"{base.rstrip('/')}{path}"
    data = json.dumps(body).encode("utf-8") if body is not None else None
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
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, resp.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    props = load_local_properties(root)
    base = props.get("onyx.baseUrl", "").strip()
    pat = props.get("onyx.pat", "").strip()

    if not base or not pat:
        print("Need onyx.baseUrl and onyx.pat in local.properties", file=sys.stderr)
        return 1

    print(f"API: {base}")
    print(f"Target refresh_freq={REFRESH_FREQ_SEC}s ({REFRESH_FREQ_SEC // 86400}d)")
    print(f"Target prune_freq={PRUNE_FREQ_SEC}s ({PRUNE_FREQ_SEC // 86400}d)")

    code, raw = api_request(base, pat, "GET", "/manage/admin/connector/status", timeout=90)
    if code != 200:
        print(f"GET connector/status failed: HTTP {code}\n{raw[:500]}", file=sys.stderr)
        return 1

    statuses = json.loads(raw)
    if not statuses:
        print("No connectors found.")
        return 0

    updated = 0
    for item in statuses:
        connector = item["connector"]
        cid = connector["id"]
        name = connector.get("name", cid)
        old_refresh = connector.get("refresh_freq")
        old_prune = connector.get("prune_freq")

        payload = {
            "name": connector["name"],
            "source": connector["source"],
            "input_type": connector["input_type"],
            "connector_specific_config": connector["connector_specific_config"],
            "refresh_freq": REFRESH_FREQ_SEC,
            "prune_freq": PRUNE_FREQ_SEC,
            "indexing_start": connector.get("indexing_start"),
            "access_type": item["access_type"],
            "groups": item.get("groups") or [],
        }

        pcode, praw = api_request(
            base,
            pat,
            "PATCH",
            f"/manage/admin/connector/{cid}",
            payload,
            timeout=90,
        )
        if pcode == 200:
            updated += 1
            print(
                f"OK {name} (id={cid}): refresh {old_refresh} -> {REFRESH_FREQ_SEC}, "
                f"prune {old_prune} -> {PRUNE_FREQ_SEC}"
            )
        else:
            print(f"FAIL {name} (id={cid}): HTTP {pcode}\n{praw[:300]}", file=sys.stderr)

    print(f"Updated {updated}/{len(statuses)} connector(s).")
    return 0 if updated == len(statuses) else 2


if __name__ == "__main__":
    raise SystemExit(main())
