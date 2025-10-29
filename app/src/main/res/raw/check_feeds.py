#!/usr/bin/env python3
"""
check_feeds.py (fast + robust)
Removes any RSS/Atom feeds from suggested_feeds.json that do not respond with
HTTP 200–399 or fail to contain valid feed XML.
Outputs a cleaned file: suggested_feeds.json
"""

import os
import json
import sys
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
from urllib.parse import urlparse

import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

# ---- Tuning knobs ----
CONNECT_TIMEOUT = 4.0
READ_TIMEOUT = 8.0
TOTAL_TIMEOUT = (CONNECT_TIMEOUT, READ_TIMEOUT)
MAX_WORKERS = min(64, (os.cpu_count() or 4) * 8)
POOL_SIZE = MAX_WORKERS
PARTIAL_BYTES = 4096        # fast sniff
FALLBACK_BYTES = 65536      # full GET cap
# A browser-y UA avoids some CDN/anti-bot blocks
USER_AGENT = (
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
)

_session = None
_session_lock = threading.Lock()

def get_session() -> requests.Session:
    global _session
    if _session is None:
        with _session_lock:
            if _session is None:
                s = requests.Session()
                retries = Retry(
                    total=2,
                    backoff_factor=0.2,
                    status_forcelist=(429, 500, 502, 503, 504),
                    allowed_methods=frozenset(["GET", "HEAD"]),
                    raise_on_status=False,
                )
                adapter = HTTPAdapter(pool_connections=POOL_SIZE, pool_maxsize=POOL_SIZE, max_retries=retries)
                s.mount("http://", adapter)
                s.mount("https://", adapter)
                _session = s
    return _session

def _decode_lower_prefix(b: bytes) -> str:
    try:
        return b.decode("utf-8", errors="ignore").lower()
    except Exception:
        return b.decode("latin-1", errors="ignore").lower()

def _looks_like_feed_text(text_lower: str) -> bool:
    # Fast XML/Atom/RSS/RDF sniff
    return ("<rss" in text_lower) or ("<feed" in text_lower) or ("<rdf" in text_lower)

def _content_type_is_xml(ct: str | None) -> bool:
    if not ct:
        return False
    ct = ct.lower()
    # common: application/rss+xml, application/atom+xml, application/xml, text/xml
    return ("xml" in ct) or ("rss" in ct) or ("atom" in ct)

def _read_prefix(resp: requests.Response, cap: int) -> bytes:
    chunk = b""
    for data in resp.iter_content(chunk_size=2048):
        if not data:
            break
        chunk += data
        if len(chunk) >= cap:
            break
    return chunk[:cap]

def _try_request(url: str, use_range: bool, cap_bytes: int) -> tuple[bool, bool]:
    """
    Returns (ok_status, looks_like_feed) for a single GET attempt.
    """
    headers = {
        "User-Agent": USER_AGENT,
        "Accept": "application/rss+xml, application/atom+xml, application/xml;q=0.9, text/xml;q=0.8, */*;q=0.5",
        "Accept-Encoding": "gzip, deflate, br",
    }
    if use_range:
        headers["Range"] = f"bytes=0-{cap_bytes-1}"

    sess = get_session()
    resp = sess.get(url, timeout=TOTAL_TIMEOUT, allow_redirects=True, headers=headers, stream=True)
    try:
        ok_status = 200 <= resp.status_code < 400
        if not ok_status:
            return False, False

        # quick accept by content-type if clearly XML (some feeds start with comments/BOM)
        ct = resp.headers.get("Content-Type")
        # still peek a bit to be safe
        prefix = _read_prefix(resp, cap_bytes)
        text = _decode_lower_prefix(prefix)
        looks_like = _looks_like_feed_text(text)

        # Be generous: if server declares XML-ish type, accept even if tags not in first cap
        if looks_like or _content_type_is_xml(ct):
            return True, True
        return True, False
    finally:
        # ensure connection is released to pool
        resp.close()

def is_feed_valid(url: str, timeout: float = READ_TIMEOUT) -> bool:
    """
    Fast path: partial GET with Range.
    Fallback: full GET (capped) without Range.
    Final grace: accept if Content-Type is XML-ish even if opening tags don't appear early.
    """
    try:
        # 1) Fast partial request
        ok_status, looks_like = _try_request(url, use_range=True, cap_bytes=PARTIAL_BYTES)
        if ok_status and looks_like:
            return True

        # 2) Fallback full GET (no Range), larger sniff window
        ok_status2, looks_like2 = _try_request(url, use_range=False, cap_bytes=FALLBACK_BYTES)
        if ok_status2 and looks_like2:
            return True

        return False
    except Exception:
        return False

def _resolve_feeds_path(adjacent_json: str = "suggested_feeds.json") -> str:
    script_dir = os.path.dirname(os.path.abspath(__file__))
    feeds_path = os.path.join(script_dir, adjacent_json)
    if not os.path.exists(feeds_path):
        alt_path = os.path.join(os.getcwd(), adjacent_json)
        if os.path.exists(alt_path):
            feeds_path = alt_path
        else:
            print("Error: suggested_feeds.json not found in script or current directory.")
            sys.exit(1)
    return feeds_path

def main():
    feeds_path = _resolve_feeds_path()
    with open(feeds_path, "r", encoding="utf-8") as f:
        feeds = json.load(f)

    # Deduplicate while preserving order
    seen = set()
    deduped = []
    for feed in feeds:
        url = (feed or {}).get("feed_url")
        if not url:
            continue
        if url not in seen:
            seen.add(url)
            deduped.append(feed)

    total = len(deduped)
    print(f"Checking {total} feeds (deduplicated from {len(feeds)}) with up to {MAX_WORKERS} workers...")

    working = []
    print_lock = threading.Lock()

    def worker(feed):
        url = feed.get("feed_url")
        ok = is_feed_valid(url)
        with print_lock:
            print(f"Testing {url} ... {'✅ OK' if ok else '❌ Invalid or unreachable'}")
        return (feed, ok)

    futures = {}
    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as ex:
        for feed in deduped:
            futures[ex.submit(worker, feed)] = feed

        for fut in as_completed(futures):
            feed, ok = fut.result()
            if ok:
                working.append(feed)

    output_path = os.path.join(os.path.dirname(feeds_path), "suggested_feeds.json")
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(working, f, indent=2, ensure_ascii=False)

    print(f"\nDone. {len(working)} valid feeds saved to {output_path}")

if __name__ == "__main__":
    main()
