# -*- coding: utf-8 -*-
'''
This file handles syncing the actual RSS/Atom feeds.
'''

from concurrent.futures import ThreadPoolExecutor, as_completed
import urllib.request
import ssl

import feedparser as fp
import socket

from datetime import datetime, timedelta
from .database import db
from sqlalchemy import distinct
from .models import FeedItem, Feed, UserFeed
from .util import (datetime_now,
                   datetuple_to_datetime)
from .cleaner import clean_entry


# Don't remove embedded videos
fp._HTMLSanitizer.acceptable_elements = \
    set(list(fp._HTMLSanitizer.acceptable_elements) + ['object',
                                                       'embed',
                                                       'iframe'])

# You should timeout if server is not responding.
# In seconds but feedparser takes about 5x longer before returning.
socket.setdefaulttimeout(1.0)


def cache_all_feeds():
    '''
    Download all feeds in database
    '''
    # First clear out old items
    delete_old_items()

    # Cpu count * 5
    with ThreadPoolExecutor() as exe:
        data_to_feed = {exe.submit(download_feed, feed, 2): feed for feed in Feed.query.all()}

        for future in as_completed(data_to_feed):
            feed = data_to_feed[future]
            try:
                data = future.result()
                cache_feed(feed, data)
            except Exception as ex:
                print("Failed to cache <{}> because: {}".format(feed.link, ex))


def download_feed(feed, timeout=2):
    '''
    Downloads a feed and returns it.

    Args:
    - feed: A Feed database model fetched from the database table
    - timeout: Timeout in seconds for blocking http actions.
    '''
    url = feed.link
    if not "://" in url:
        url = "http://" + url

    print("Downloading <{}>".format(url))

    # Allow self-signed certificates
    myssl = ssl.create_default_context()
    myssl.check_hostname=False
    myssl.verify_mode = ssl.CERT_NONE

    with urllib.request.urlopen(url, timeout=timeout, context=myssl) as conn:
        return conn.read()


def cache_feed(feed, data):
    '''
    Parse the feed
    '''
    # Parse the result
    rss = fp.parse(data)

    # The feed object
    f = rss.feed

    # If feed does not have title,
    # which is a required attribute,
    # we skip it
    if not hasattr(f, "title"):
        try:
            print(rss.debug_message)
        except:
            pass
        return

    # If no items, there is nothing to do
    if len(rss.entries) == 0:
        try:
            print("No new items, ignoring {}".format(f.title))
        except:
            print("No new items, ignoring title which I can't print")
        return

    # Update feed timestamp
    timestamp = datetime_now()

    # Get individual entries
    any_new_items = False
    for item in rss.entries:
        # Guid is exists, otherwise link
        guid = item.get("id", item.link)
        # Ignore existing
        if db.session.query(FeedItem.query.
                            filter(FeedItem.feed_id == feed.id,
                                   FeedItem.guid == guid).
                            exists()).scalar():
            continue

        any_new_items = True
        # Fill with cleaned data
        feeditem = FeedItem()
        feeditem.guid = guid
        feeditem.feed_id = feed.id
        feeditem.timestamp = timestamp
        clean_entry(feeditem, item)
        # If published is none, use timestamp
        if feeditem.published is None:
            feeditem.published = timestamp
        # Add to database (commit later)
        db.session.add(feeditem)

    # Update feed only if any new items were added
    if any_new_items:
        feed.timestamp = timestamp
        feed.title = f.title
        feed.description = f.get("description", "")
        feed.published = datetuple_to_datetime(f.get("published_parsed",
                                                     None))
        if feed.published is None:
            feed.published = timestamp
        feed.etag = rss.get("etag", None)
        feed.modified = rss.get("modified", None)

        try:
            print("Cached:", feed.title)
        except:
            print("Cached something with a weird title I can't print")

        # Add feed to database
        db.session.add(feed)
        # And commit all
        db.session.commit()
    else:
        try:
            print("No new items in {}".format(feed.title))
        except:
            print("No new items in erroneous title")


def delete_old_items(days=14):
    '''
    Clear out old items so the database doesn't grow too much.
    By default, items are deleted when they are 14 days old.
    After this a 'vacuum' is run which removes excess database
    pages.

    Args:
    days - By default items older than [14] days are deleted
    '''
    dt = datetime.utcnow() - timedelta(days=days)

    FeedItem.query.filter(FeedItem.timestamp < dt).delete()
    db.session.commit()

    db.engine.execute("vacuum;")


def delete_orphan_feeds():
    '''
    Delete feeds which are no longer connected to any users.
    '''
    # Get all unique feed_ids connected by users
    user_feed_ids = db.session.query(distinct(UserFeed.feed_id)).all()
    # Convert from ((1,), (2,)) to [1, 2]
    user_feed_ids = [x[0] for x in user_feed_ids]
    # Now get all feeds not in that list
    feeds = Feed.query.filter(~Feed.id.in_(user_feed_ids)).all()
    # And remove them
    if len(feeds) > 0:
        for feed in feeds:
            db.session.delete(feed)
        db.session.commit()
