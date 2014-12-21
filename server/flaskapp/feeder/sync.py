# -*- coding: utf-8 -*-
'''
This file handles syncing the actual RSS/Atom feeds.
'''

import feedparser as fp
from feeder import db
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


class AttributeDict(dict):
    __getattr__ = dict.__getitem__
    __setattr__ = dict.__setitem__


def cache_all_feeds():
    '''
    Download all feeds in database
    '''
    for feed in Feed.query.all():
        cache_feed(feed)


def get_fresh_feed(url, etag=None, modified=None):
    if "://" not in url:
        url = "http://" + url

    # Parse the result
    rss = fp.parse(url, etag=etag, modified=modified)

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
        return None

    # Update feed timestamp
    timestamp = datetime_now()

    feed = Feed(title=f.title,
                description=f.get("description", ""),
                link=url,
                timestamp=timestamp)
    feed.published = datetuple_to_datetime(f.get("published_parsed",
                                                 None))
    if feed.published is None:
        feed.published = timestamp
    feed.etag = rss.get("etag", None)
    feed.modified = rss.get("modified", None)

    feed.items = []

    # Get individual entries
    for item in rss.entries:
        # Fill with cleaned data
        feeditem = FeedItem()
        # feeditem.feed_id = feed.id
        feeditem.timestamp = timestamp
        clean_entry(feeditem, item)
        # If published is none, use timestamp
        if feeditem.published is None:
            feeditem.published = timestamp

        # Add to feed
        feed.items.append(feeditem)

    print("Returning etag:", etag, ", modified:", modified)

    return feed


def cache_feed(feed):
    '''
    Download the feed.
    '''
    url = feed.link
    if "://" not in url:
        url = "http://" + url

    # Parse the result
    rss = fp.parse(url, etag=feed.etag, modified=feed.modified)

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
        print("No new items, ignoring {}".format(f.title))
        return

    # Update feed timestamp
    timestamp = datetime_now()

    # Get individual entries
    any_new_items = False
    for item in rss.entries:
        # Ignore existing
        if db.session.query(FeedItem.query.
                            filter(FeedItem.feed_id == feed.id,
                                   FeedItem.link == item.link).
                            exists()).scalar():
            continue

        any_new_items = True
        # Fill with cleaned data
        feeditem = FeedItem()
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

        print("Cached:", feed.title)

        # Add feed to database
        db.session.add(feed)
        # And commit all
        db.session.commit()
    else:
        print("No new items in {}".format(feed.title))


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
