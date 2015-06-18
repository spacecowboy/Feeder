# -*- coding: utf-8 -*-
from ..sync import parse_feed as cache_feed
from ..database import GraphDB

from .feeds import testfeed, testfeed_link, testfeed_updated

import feedparser as fp
import json


def test_cache_feed(graph):
    # Local rss file
    db = GraphDB(graph=graph)
    # Make sure user exists
    email = 'bob@bob.com'
    db.merge_user(email)
    # First subscribe
    db.subscribe(email, testfeed_link)

    rss = fp.parse(testfeed)

    cache_feed(db, rss)


    res = graph.cypher.execute("MATCH (:Feed {{link: {} }})<-[:IN]-(item:Item)\nRETURN COUNT(item) as count".format(json.dumps(testfeed_link)))
    assert len(res) == 1
    assert res[0]['count'] == 2

    # Make sure feed stuff are correct
    res = graph.cypher.execute("MATCH (feed:Feed {{link: {} }})\nRETURN feed".format(json.dumps(testfeed_link)))

    f = res[0]['feed']
    print(rss.feed)
    print(f)
    assert f['title'] == "Cowboy Programmer"
    assert f['description'] == "Ramblings about stuff."

    # Sync again should make no difference, as the guids are the same
    cache_feed(db, rss)

    res = graph.cypher.execute("MATCH (:Feed {{link: {} }})<-[:IN]-(item:Item)\nRETURN COUNT(item) as count".format(json.dumps(testfeed_link)))
    assert len(res) == 1
    assert res[0]['count'] == 2


def test_cache_updated(graph):
    db = GraphDB(graph=graph)

    rss = fp.parse(testfeed_updated)

    cache_feed(db, rss)
    res = graph.cypher.execute("MATCH (:Feed {{link: {} }})<-[:IN]-(item:Item)\nRETURN COUNT(item) as count".format(json.dumps(testfeed_link)))
    assert len(res) == 1
    # There is one new item, and one item was updated
    assert res[0]['count'] == 3
