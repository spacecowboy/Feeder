# -*- coding: utf-8 -*-
'''
Wrappers for databases
'''

from py2neo import Graph
from .cyphers import (get_subscribed_feeds, cleanup_items,
                      on_synced, merge_user, subscribe,
                      get_user, get_users_new_feeditems,
                      get_users_new_unsubscribes,
                      get_feed_and_items, unsubscribe,
                      feed_constraints, user_constraints)


_db = None


def get_database(url=None):
    '''
    Gets a database object singleton
    '''
    global _db
    if _db is None:
        if url is None:
            from aggregator import app
            url = app.config.get('FEEDER_NEO_URL', None)

        _db = GraphDB(url=url)

    return _db


class GraphDB(object):
    def __init__(self, url=None, graph=None):
        # Connect to neo
        if graph is None:
            self.graph = Graph() if url is None else Graph(url)
        else:
            self.graph = graph
        # Make sure we have constraints
        self.graph.cypher.execute(feed_constraints())
        self.graph.cypher.execute(user_constraints())

    def merge_user(self, email, pwhash=None):
        res = self.graph.cypher.execute(merge_user(email, pwhash))
        return res[0]['user']

    def get_user(self, email):
        res = self.graph.cypher.execute(get_user(email))
        return res[0]['user']

    def subscribe(self, email, link, usertitle=None, usertag=None):
        res = self.graph.cypher.execute(subscribe(email, link,
                                                  usertitle, usertag))
        return res[0]['feed'], res[0]['subscription']

    def unsubscribe(self, email, link):
        self.graph.cypher.execute(unsubscribe(email, link))

    def get_subscribed_feeds(self):
        res = self.graph.cypher.execute(get_subscribed_feeds())
        return [r['feed'] for r in res]

    def cleanup_items(self, link):
        self.graph.cypher.execute(cleanup_items(link))

    def on_synced(self, feed, timestamp, items):
        self.graph.cypher.execute(on_synced(feed, timestamp, items))

    def get_feed_and_items(self, link, limit=10):
        res = self.graph.cypher.execute(get_feed_and_items(link, limit))
        return res[0]['feed'], res[0]['items']

    def get_users_new_feeditems(self, email, lastsync=None):
        if lastsync is None:
            lastsync = 0

        res = self.graph.cypher.execute(get_users_new_feeditems(email,
                                                                lastsync))
        return res

    def get_users_new_unsubscribes(self, email, lastsync=None):
        if lastsync is None:
            lastsync = 0

        res = self.graph.cypher.execute(get_users_new_unsubscribes(email,
                                                                   lastsync))
        return res
