"""
check_feeds.py
Removes any RSS/Atom feeds from suggested_feeds.json that do not respond with
HTTP 200–399 or fail to contain valid feed XML.
Outputs a cleaned file: working_feeds.json
"""

import os
import json
import sys
import requests
from urllib.parse import urlparse

def is_feed_valid(url: str, timeout: float = 8.0) -> bool:
    """Check if a feed URL returns a valid XML-based feed."""
    try:
        resp = requests.get(url, timeout=timeout, allow_redirects=True, headers={
            "User-Agent": "FeedChecker/1.0"
        })
        if not (200 <= resp.status_code < 400):
            return False
        text = resp.text[:2048].lower()
        return any(tag in text for tag in ("<rss", "<feed", "<rdf"))
    except Exception:
        return False

def main():
    # Path resolution: find suggested_feeds.json next to this script
    script_dir = os.path.dirname(os.path.abspath(__file__))
    feeds_path = os.path.join(script_dir, "suggested_feeds.json")

    # But allow running from elsewhere: look in current dir if missing
    if not os.path.exists(feeds_path):
        alt_path = os.path.join(os.getcwd(), "suggested_feeds.json")
        if os.path.exists(alt_path):
            feeds_path = alt_path
        else:
            print("Error: suggested_feeds.json not found in script or current directory.")
            sys.exit(1)

    with open(feeds_path, "r", encoding="utf-8") as f:
        feeds = json.load(f)

    print(f"Checking {len(feeds)} feeds...")
    working = []

    for feed in feeds:
        url = feed.get("feed_url")
        if not url:
            print(f"Skipping empty URL for {feed.get('title')}")
            continue
        print(f"Testing {url} ... ", end="", flush=True)
        if is_feed_valid(url):
            print("✅ OK")
            working.append(feed)
        else:
            print("❌ Invalid or unreachable")

    output_path = os.path.join(os.path.dirname(feeds_path), "suggested_feeds.json")
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(working, f, indent=2, ensure_ascii=False)

    print(f"\nDone. {len(working)} valid feeds saved to {output_path}")

if __name__ == "__main__":
    main()
