# -*- coding: utf-8 -*-
'''
This file handles syncing the actual RSS/Atom feeds.
'''

import feedparser as fp
from .util import timestamp, datetuple_to_timestamp
from .database import get_database
from .cleaner import get_clean_entry

# Don't remove embedded videos
fp._HTMLSanitizer.acceptable_elements = \
    set(list(fp._HTMLSanitizer.acceptable_elements) + ['object',
                                                       'embed',
                                                       'iframe'])


def cache_all_feeds():
    '''
    Download all feeds in database
    '''
    db = get_database()
    for feed in db.get_subscribed_feeds():
        cache_feed(db, feed)
        db.cleanup_items(feed['link'])


def cache_feed(db, feed):
    '''
    Download one specified feed
    '''
    url = feed['link']
    if "://" not in url:
        url = "http://" + url

    # Download and parse feed
    rss = fp.parse(url, etag=feed['etag'], modified=feed['modified'])
    # Feed might consider itself to be under a different link
    # Keep existing for identification purposes
    rss.feed.link = feed['link']
    parse_feed(db, rss)


def parse_feed(db, rss):
    '''
    Given a parsed rss object, cache the items.
    '''
    # The feed object
    f = rss.feed

    # Title is a required attribute
    if not hasattr(f, "title"):
        try:
            print(rss.debug_message)
        except:
            pass
        print("No title, exiting...")
        return

    # Need current timestamp
    ts = timestamp()

    # Update feed
    feed = {}
    feed['link'] = f.link
    feed['title'] = f.title
    feed['description'] = f.get("description", "")
    feed['published'] = datetuple_to_timestamp(f.get("published_parsed",
                                               None))
    if feed['published'] is None:
        feed['published'] = ts
    feed['etag'] = rss.get("etag", None)
    feed['modified'] = rss.get("modified", None)

    feeditems = []
    for item in rss.entries:
        feeditem = get_clean_entry(item, timestamp=ts)
        feeditems.append(feeditem)

    db.on_synced(feed, ts, feeditems)
    print("Cached:", feed['title'], "(", len(feeditems), ")")
