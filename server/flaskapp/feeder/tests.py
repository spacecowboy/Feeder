# -*- coding: utf-8 -*-
'''
python -m feeder.tests
'''

import unittest
from .sync import cache_all_feeds, delete_orphan_feeds
from .models import (User, Feed, UserFeed, FeedItem,
                     get_user, get_feed, get_userfeed)
from feeder import db


def setUpModule():
    # Create schema
    db.create_all()

    # Two users
    bob = get_user("bob@bob.bob")
    frank = get_user("frank@frank.frank")

    # Some feeds
    cowboy = get_feed("cowboyprogrammer.org/rss/")
    bubbla = get_feed("bubb.la/rss.xml")
    slashdot = get_feed("http://rss.slashdot.org/Slashdot/slashdot")
    cornu = get_feed("http://feeds.feedburner.com/cornubot")
    torrent = get_feed("http://kickass.to/tv/?rss=1")

    # bob likes three
    bobfeeds = []
    get_userfeed(bob, cowboy, "android", "Cowboy")
    get_userfeed(bob, bubbla, "news", "Bubbla")
    get_userfeed(bob, torrent, None, "torrents")
    # frank likes three
    frankfeeds = []
    get_userfeed(frank, cowboy, "mobile", "CBProgrammer")
    get_userfeed(frank, slashdot, "News")
    get_userfeed(frank, cornu)

    # Cache feeds
    print("Caching feeds...")
    cache_all_feeds()
    print("Cache completed")


def tearDownModule():
    db.session.rollback()
    db.drop_all()
    #pass


class CacheTests(unittest.TestCase):

    def test_torrents(self):
        bob = get_user("bob@bob.bob")
        bobfeeds = bob.feeds.all()
        for uf in bobfeeds:
            if "torrent" not in uf.title:
                continue
            for item in uf.feed.items.all():
                # Torrens have enclosures
                self.assertIsNotNone(item.enclosure)

    def test_got_items(self):
        items = FeedItem.query.all()
        self.assertTrue(len(items) > 0)

    def test_assumptions(self):
        for feed in Feed.query.all():
            prev_pub = None
            for item in feed.items.all():
                # They should be ordered by time
                if prev_pub is not None:
                    self.assertTrue(prev_pub >= item.published)
                prev_pub = item.published

                # Should exist title, dates, and snippets
                #print(item.title, item.published)
                self.assertIsNotNone(item.title)
                self.assertIsNotNone(item.title_stripped)
                self.assertIsNotNone(item.snippet)
                self.assertIsNotNone(item.description)
                self.assertIsNotNone(item.published)
                self.assertIsNotNone(item.timestamp)

                # Timestamp must be less than feed's
                self.assertTrue(feed.timestamp >= item.timestamp)

    def test_bob(self):
        bob = get_user("bob@bob.bob")
        feeds = bob.feeds.all()
        self.assertEqual(3, len(feeds))
        for uf in feeds:
            if "cowboy" in uf.link:
                self.assertEqual("Cowboy", uf.title)
                self.assertEqual("android", uf.tag)
                self.assertNotEqual(uf.title, uf.feed.title)
            elif "bubb.la" in uf.link:
                self.assertEqual("Bubbla", uf.title)
                self.assertEqual("news", uf.tag)
                self.assertNotEqual(uf.title, uf.feed.title)
            elif "kick" in uf.link:
                self.assertEqual("torrents", uf.title)
                self.assertIsNone(uf.tag)
                self.assertNotEqual(uf.title, uf.feed.title)

    def test_z_delete_user_cascade(self):
        # Delete user should delete connected user feeds
        frank = get_user("frank@frank.frank")
        frankfeeds = frank.feeds.all()
        userfeeds = UserFeed.query.all()

        prev_len = len(userfeeds)
        expected_len = prev_len - len(frankfeeds)

        db.session.delete(frank)
        db.session.commit()

        userfeeds = UserFeed.query.all()

        self.assertEqual(expected_len, len(userfeeds))

    def test_z_delete_feed_cascade(self):
        # Deleting a feed should cascade and delete all feeditems
        feed = Feed.query.first()

        fid = feed.id

        targetlen = FeedItem.query.filter_by(feed_id=fid).count()
        prev_len = FeedItem.query.count()

        db.session.delete(feed)
        db.session.commit()

        new_len = FeedItem.query.count()

        self.assertEqual(new_len, prev_len - targetlen)

    def test_z_delete_orphan_feeds(self):
        # Test method which deletes feeds not connected to any user
        # Get number of existing feeds
        feeds_count = Feed.query.count()
        # Get count of frank's feeds
        frank = get_user("frank@frank.frank")
        franks_count = UserFeed.query.filter_by(user_id=frank.id).count()
        # He shares one feed with bob, so subtract that
        franks_count -= 1
        # Remove frank
        db.session.delete(frank)
        db.session.commit()

        # Call clean up function
        delete_orphan_feeds()

        # Now just check the results
        self.assertEqual(Feed.query.count(), feeds_count - franks_count)

if __name__ == '__main__':
    unittest.main()
