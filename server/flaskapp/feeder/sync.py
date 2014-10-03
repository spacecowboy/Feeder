# -*- coding: utf-8 -*-
'''
This file handles syncing the actual RSS/Atom feeds.
'''

import feedparser as fp
from feeder import db
from .models import FeedItem, Feed
from .util import (datetime_now,
                   datetuple_to_datetime)
from .cleaner import clean_entry


# Don't remove embedded videos
fp._HTMLSanitizer.acceptable_elements = \
    set(list(fp._HTMLSanitizer.acceptable_elements) + ['object',
                                                       'embed',
                                                       'iframe'])


def cache_all_feeds():
    '''
    Download all feeds in database
    '''
    for feed in Feed.query.all():
        cache_feed(feed)


def cache_feed(feed):
    '''
    Download the feed.
    '''
    url = feed.link
    if not "://" in url:
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
        # Add to database (commit later)
        db.session.add(feeditem)

    # Update feed only if any new items were added
    if any_new_items:
        feed.timestamp = timestamp
        feed.title = f.title
        feed.description = f.get("description", "")
        feed.published = datetuple_to_datetime(f.get("published_parsed",
                                                     None))
        feed.etag = rss.get("etag", None)
        feed.modified = rss.get("modified", None)

        print("Cached:", feed.title)

        # Add feed to database
        db.session.add(feed)
        # And commit all
        db.session.commit()
    else:
        print("No new items in {}".format(feed.title))

    def delete_orphan_feeds(self):
        '''
        Delete feeds which are no longer connected to any users.
        '''
        # TODO
        pass
