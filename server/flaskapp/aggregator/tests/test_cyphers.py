# -*- coding: utf-8 -*-
"""
Test the database operations.
"""
import pytest
from ..util import timestamp
from ..cyphers import *
import json
from ..cleaner import make_unescaped_item, make_unescaped_feed


_bob = "bob@bobs.com"
_frank = "frank@bobs.com"

_feed1_link = 'http://site.com/rss'
_feed2_link = 'http://another.site.com/rss'
_feed3_link = 'http://yetonemore.site.com/rss'


def test_getuser_notregistered(graph):
    user = graph.cypher.execute_one(get_user('nothere@invisible.com'))
    assert user is None


def test_adduser(graph):
    '''
    A user account is required for everything else.
    '''
    user = graph.cypher.execute_one(merge_user(_bob, None))
    assert user is not None
    assert user['email'] == _bob
    assert user['passwordhash'] is None


def test_getuser_bob(graph):
    user = graph.cypher.execute_one(get_user(_bob))
    assert user is not None
    assert user['email'] == _bob
    assert user['passwordhash'] is None


def test_adduser_with_password(graph):
    '''
    A user account is required for everything else.
    '''
    pwhash = 'ab292asva23'
    user = graph.cypher.execute_one(merge_user(_frank, pwhash))
    assert user is not None
    assert user['email'] == _frank
    assert user['passwordhash'] == pwhash


def test_getuser_frank(graph):
    user = graph.cypher.execute_one(get_user(_frank))
    assert user is not None
    assert user['email'] == _frank
    assert user['passwordhash'] is not None


def test_unsubscribe_nonexisting(graph):
    res = graph.cypher.execute(unsubscribe(_bob, "http://somelink.com/rss"))
    assert len(res) == 0


def test_subscribe(graph):
    '''
    Subscription is done before caching.
    '''
    # Getting user's feeds should return nothing yet
    res = graph.cypher.execute(get_user_feeds(_bob))
    assert len(res) == 0

    # Subscribe
    res = graph.cypher.execute(subscribe(_bob, _feed1_link, None, None))
    assert len(res) == 1

    sub = res[0]['subscription']
    assert sub['timestamp'] > 0
    assert sub['usertitle'] is None
    assert sub['usertag'] is None

    feed = res[0]['feed']
    assert feed['link'] == _feed1_link

    # Getting user's feeds should return a single item
    res = graph.cypher.execute(get_user_feeds(_bob))
    assert len(res) == 1

    # Re-subscribe

    res = graph.cypher.execute(subscribe(_bob, _feed1_link,
                                         'mytitle', 'mytag'))
    assert len(res) == 1

    sub2 = res[0]['subscription']
    assert sub2['timestamp'] >= sub['timestamp']
    assert sub2['usertitle'] == 'mytitle'
    assert sub2['usertag'] == 'mytag'

    # Getting user's feeds should return a single item still
    res = graph.cypher.execute(get_user_feeds(_bob))
    assert len(res) == 1

    # Getting all feeds and items should only be one
    res = graph.cypher.execute(get_users_new_feeditems(_bob, 0))
    print(res)
    assert len(res) == 1
    assert res[0]['feed']['link'] == _feed1_link

    # Subscribe Frank
    res = graph.cypher.execute(subscribe(_frank, _feed1_link, None, None))
    assert len(res) == 1

    sub = res[0]['subscription']
    assert sub['timestamp'] > 0
    assert sub['usertitle'] is None
    assert sub['usertag'] is None

    res = graph.cypher.execute(subscribe(_frank, _feed2_link, None, None))
    assert len(res) == 1

    sub = res[0]['subscription']
    assert sub['timestamp'] > 0
    assert sub['usertitle'] is None
    assert sub['usertag'] is None

    # Getting all feeds and items should be two
    res = graph.cypher.execute(get_users_new_feeditems(_frank, 0))
    print(res)
    assert len(res) == 2
    # Order is not fixed
    assert res[0]['feed']['link'] in [_feed1_link, _feed2_link]
    assert res[1]['feed']['link'] in [_feed1_link, _feed2_link]


def test_get_subscribed_feeds(graph):
    res = graph.cypher.execute(get_subscribed_feeds())
    assert len(res) == 2

    # Should be 1 if Frank unsubscribes to two
    graph.cypher.execute(unsubscribe(_frank, _feed2_link))

    res = graph.cypher.execute(get_subscribed_feeds())
    assert len(res) == 1

    # Resubscribe
    graph.cypher.execute(subscribe(_frank, _feed2_link, None, None))


def test_synced_none(graph):
    # Sync returned no items
    ts = timestamp()
    items = []
    f = make_unescaped_feed(ts, _feed1_link, 'Feed1', 'Feed1 desc', ts)

    graph.cypher.execute(on_synced(f, ts, items))


def test_synced_one(graph):
    # Sync returned one item
    ts = timestamp()
    items = []
    for i in range(1):
        si = str(i)
        items.append(make_unescaped_item(ts, 'guid' + si,
                               _feed1_link + '/item/' + si,
                               'title' + si,
                               'descrip' + si,
                               'titlestrip' + si,
                               'snippet' + si,
                               ts - (1+i)))

    f = make_unescaped_feed(ts, _feed1_link, 'Feed1', 'Feed1 desc', ts)

    graph.cypher.execute(on_synced(f, ts, items))

    # Get all feeds and items
    res = graph.cypher.execute(get_users_new_feeditems(_frank, 0))
    # Two feeds
    assert len(res) == 2

    for r in res:
        # Feed and items
        assert len(r) == 2
        assert r['feed'] is not None
        assert r['items'] is not None

        feed = r['feed']
        items = r['items']
        if feed['link'] == _feed1_link:
            # First feed has ONE item
            assert len(items) == 1
        else:
            # Second feed has no items
            assert len(items) == 0


def test_synced_many(graph):
    # Sync return some new items
    ts = timestamp()

    items = []
    count = 10
    f = make_unescaped_feed(ts, _feed1_link, 'Feed1', 'Feed1 desc', ts)

    for i in range(count - 4):
        si = str(i)
        its = ts - 1000 * (1 + i)
        items.append(make_unescaped_item(its,
                               'guid' + si,
                               _feed1_link + '/item/' + si,
                               'title' + si,
                               'descrip' + si,
                               'titlestrip' + si,
                               'snippet' + si,
                               its))

    # These are old items
    graph.cypher.execute(on_synced(f, ts - 50000, items))

    items = []
    for i in range(count - 4, count):
        si = str(i)
        its = ts - (1 + i)
        items.append(make_unescaped_item(its,
                               'guid' + si,
                               _feed1_link + '/item/' + si,
                               'title' + si,
                               'descrip' + si,
                               'titlestrip' + si,
                               'snippet' + si,
                               its))

    # Newest items
    graph.cypher.execute(on_synced(f, ts, items))

    # And then the user also unsubscribes to feed 3!
    graph.cypher.execute(subscribe(_frank, _feed3_link, None, None))
    graph.cypher.execute(unsubscribe(_frank, _feed3_link))
    # Which also got some items
    f = make_unescaped_feed(ts, _feed3_link, 'Feed3', 'Feed3 desc', ts)

    items = []
    for i in range(7):
        si = str(i)
        its = ts - 1000*(1 + i)
        items.append(make_unescaped_item(its,
                               'f3_guid' + si,
                               _feed3_link + '/item/' + si,
                               'title' + si,
                               'descrip' + si,
                               'titlestrip' + si,
                               'snippet' + si,
                               its))
    graph.cypher.execute(on_synced(f, ts, items))

    # Get all feeds and items
    res = graph.cypher.execute(get_users_new_feeditems(_frank, 0))
    # Two feeds
    assert len(res) == 2

    for r in res:
        # Feed and items
        assert len(r) == 2
        assert r['feed'] is not None
        assert r['items'] is not None

        feed = r['feed']
        items = r['items']
        if feed['link'] == _feed1_link:
            # First feed has X items
            assert len(items) == count
        elif feed['link'] == _feed2_link:
            # Second feed has no items
            assert len(items) == 0
        else:
            # Third feed is an unsubscription
            assert len(items) == 0
            assert 0

    # Now filter items by timestamp
    # Query should only return 4 items now
    lastsync = ts - 50000

    # Get all feeds and items
    res = graph.cypher.execute(get_users_new_feeditems(_frank, lastsync))
    # Two feeds
    assert len(res) == 2

    for r in res:
        # Feed and items
        assert len(r) == 2
        assert r['feed'] is not None
        assert r['items'] is not None

        feed = r['feed']
        items = r['items']
        if feed['link'] == _feed1_link:
            # First feed has X items
            assert len(items) == 4
            for i in items:
                assert i['timestamp'] > lastsync
        else:
            # Second feed has no items
            assert len(items) == 0

    # And then we have one unsubscription
    res = graph.cypher.execute(get_users_new_unsubscribes(_frank, lastsync))
    assert len(res) == 1
    assert res[0]['feed']['link'] == _feed3_link


def test_get_feed_and_items(graph):
    lim = 3
    res = graph.cypher.execute(get_feed_and_items(_feed1_link, lim))

    assert len(res) == 1
    assert res[0]['feed'] is not None
    assert res[0]['items'] is not None

    assert res[0]['feed']['link'] == _feed1_link
    assert res[0]['feed']['title'] is not None

    items = res[0]['items']
    assert len(items) == lim


def test_cleanup(graph):
    # No need to keep very old items, so clear them out
    # I want to remove items which are older than X
    # IF they are not part of the newest Y items

    # First, no items to delete
    flink = 'http://cleanup.com/rss'

    res = graph.cypher.execute(cleanup_items(flink))
    print(res)
    assert len(res) == 1
    assert res[0]['deleted'] == 0

    ts = timestamp() - (365 * 24 * 3600 * 1000)

    f = make_unescaped_feed(ts, flink, 'FeedCleanup', 'Feed desc', ts)

    # Every item is old enough to be targeted for termination
    # But keep 100 of them
    items = []
    count = 138
    for i in range(count):
        si = str(i)
        its = ts
        items.append(make_unescaped_item(its,
                               'fclean_guid' + si,
                               flink + '/item/' + si,
                               'title' + si,
                               'descrip' + si,
                               'titlestrip' + si,
                               'snippet' + si,
                               its))
    graph.cypher.execute(on_synced(f, ts, items))

    # Now clean up should delete past 100 first items
    res = graph.cypher.execute(cleanup_items(flink))
    print(res)
    assert len(res) == 1
    assert res[0]['deleted'] == (count - 100)

    # Add some items which are 'too new'
    ts = timestamp() - (3 * 24 * 3600 * 1000)

    items = []
    countnew = 23
    for i in range(count, count + countnew):
        si = str(i)
        its = ts
        items.append(make_unescaped_item(its,
                               'fclean2_guid' + si,
                               flink + '/item/' + si,
                               'title' + si,
                               'descrip' + si,
                               'titlestrip' + si,
                               'snippet' + si,
                               its))
    graph.cypher.execute(on_synced(f, ts, items))

    # Now clean up should delete more old items
    res = graph.cypher.execute(cleanup_items(flink))
    print(res)
    assert len(res) == 1
    assert res[0]['deleted'] == countnew

    # Now add even more too new items
    items = []
    countlast = 150
    for i in range(countlast):
        si = str(i)
        its = ts
        items.append(make_unescaped_item(its,
                               'fclean3_guid' + si,
                               flink + '/item/' + si,
                               'title' + si,
                               'descrip' + si,
                               'titlestrip' + si,
                               'snippet' + si,
                               its))
    graph.cypher.execute(on_synced(f, ts, items))

    # Now clean up should delete more old items
    res = graph.cypher.execute(cleanup_items(flink))
    print(res)
    assert len(res) == 1
    # Deletes last of old items
    assert res[0]['deleted'] == (100 - countnew)


def test_guid_conflict(graph):
    # Two items in two different feeds having the same guid
    # must be possible
    ts = timestamp()

    guid = 'guid123adfkilbvjqeo'

    f1 = make_unescaped_feed(ts, "http://feedfirst.com/rss", 'FeedA', 'FeedA', ts)

    items1 = [make_unescaped_item(ts,
                        guid,
                        f1['link'] + '/item/',
                        'titleA',
                        'descrip',
                        'titlestrip',
                        'snippet',
                        ts)]

    # Save first
    graph.cypher.execute(on_synced(f1, ts, items1))

    f2 = make_unescaped_feed(ts, "http://feedsecond.com/rss", 'FeedB', 'FeedB', ts)

    items2 = [make_unescaped_item(ts,
                        guid,
                        f2['link'] + '/item/',
                        'titleB',
                        'descrip',
                        'titlestrip',
                        'snippet',
                        ts)]

    # Save second
    graph.cypher.execute(on_synced(f2, ts, items2))

    res = graph.cypher.execute("MATCH (i:Item {{ guid: {} }})\n".format(json.dumps(guid)) +
                               "RETURN COUNT(i) as count")

    assert len(res) == 1
    assert res[0]['count'] == 2
