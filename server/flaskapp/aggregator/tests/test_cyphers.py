# -*- coding: utf-8 -*-
"""
Test the database operations.
"""
import pytest
from time import time
from ..cyphers import *
from ..models import make_item, make_feed


_bob = "bob@bobs.com"
_frank = "frank@bobs.com"

_feed1_link = 'http://site.com/rss'
_feed2_link = 'http://another.site.com/rss'


def test_escapedict():
    # One dict as arg
    res = escapedict(dict(a='a', b='b'))
    assert res['a'] == '"a"'
    assert res['b'] == '"b"'

    # One dict as kwargs
    res = escapedict(a='a', b='b')
    assert res['a'] == '"a"'
    assert res['b'] == '"b"'

    # Several dicts
    res = escapedict(dict(a='a', b='b'),
                     dict(c='c', d='d'))
    assert len(res) == 2
    assert res[0]['a'] == '"a"'
    assert res[0]['b'] == '"b"'
    assert res[1]['c'] == '"c"'
    assert res[1]['d'] == '"d"'


def test_escaped():

    @escaped
    def mytest(arg):
        return arg

    assert '"bob"' == mytest('bob')
    assert 'null' == mytest(None)


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
    ts = int(time())
    items = []
    f = make_feed(ts, _feed1_link, 'Feed1', 'Feed1 desc', ts)

    graph.cypher.execute(on_synced(f, ts, items))


def test_synced_one(graph):
    # Sync returned one item
    ts = int(time())
    items = []
    for i in range(1):
        si = str(i)
        items.append(make_item(ts, 'guid' + si,
                               _feed1_link + '/item/' + si,
                               'title' + si,
                               'descrip' + si,
                               'titlestrip' + si,
                               'snippet' + si,
                               ts - (1+i)))

    f = make_feed(ts, _feed1_link, 'Feed1', 'Feed1 desc', ts)

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
    ts = int(time())

    items = []
    count = 10
    f = make_feed(ts, _feed1_link, 'Feed1', 'Feed1 desc', ts)

    for i in range(count - 4):
        si = str(i)
        its = ts - (1 + i)
        items.append(make_item(its,
                               'guid' + si,
                               _feed1_link + '/item/' + si,
                               'title' + si,
                               'descrip' + si,
                               'titlestrip' + si,
                               'snippet' + si,
                               its))

    # These are old items
    graph.cypher.execute(on_synced(f, ts - 100, items))

    items = []
    for i in range(count - 4, count):
        si = str(i)
        its = ts - (1 + i)
        items.append(make_item(its,
                               'guid' + si,
                               _feed1_link + '/item/' + si,
                               'title' + si,
                               'descrip' + si,
                               'titlestrip' + si,
                               'snippet' + si,
                               its))

    # Newest items
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
        else:
            # Second feed has no items
            assert len(items) == 0

    # Now filter items by timestamp
    # Query should only return 4 items now
    lastsync = ts - 50
    print("lastsync", lastsync)

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
            print([x['timestamp'] for x in items])
            assert len(items) == 4
            for i in items:
                assert i['timestamp'] > lastsync
        else:
            # Second feed has no items
            assert len(items) == 0


def test_cleanup(graph):
    # No need to keep very old items, so clear them out
    # I want to remove items which are older than X
    # IF they are not part of the newest Y items
    graph.cypher.execute(cleanup_items())
    print("TODO check")
    assert 0


def test_milliseconds(graph):
    print("Neo timestamp uses milliseconds and python uses seconds")
    assert 0
