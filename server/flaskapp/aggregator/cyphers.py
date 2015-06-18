# -*- coding: utf-8 -*-
"""
Cypher queries to be used with Neo4j.
"""

from .util import escaped, escapedict


@escaped
def merge_user(email, pwhash):
    cph = "\n".join(("MERGE (u:User {{ email: {email} }})",
                     "ON CREATE SET u.passwordhash = {pwhash}",
                     "return u as user"))
    return cph.format(email=email, pwhash=pwhash)


@escaped
def get_user(email):
    return """MATCH (u:User {{ email: {} }})
              RETURN u as user""".format(email)


@escaped
def unsubscribe(email, link):
    s = """\
    MATCH (u:User {{ email: {ml} }}), (f:Feed {{link: {fl} }})
    CREATE UNIQUE (u)-[:UNSUBSCRIBED {{ timestamp: timestamp() }}]->(f)
    WITH u, f
    OPTIONAL MATCH (u)-[s:SUBSCRIBES]->(f)
    DELETE s""".format(ml=email, fl=link)
    return s


@escaped
def subscribe(email, link, usertitle, usertag):
    '''
    Might create a new feed, hence merge.
    '''
    s = """\
    MATCH (u:User {{ email: {ml} }})
    MERGE (f:Feed {{ link: {fl} }})
    ON CREATE SET f.timestamp = timestamp()
    MERGE (u)-[s:SUBSCRIBES]->(f)
    SET s.timestamp = timestamp(), s.usertitle = {usertitle}, \
s.usertag = {usertag}
    WITH u, f, s
    OPTIONAL MATCH (u)-[us:UNSUBSCRIBED]->(f)
    DELETE us
    RETURN f as feed, s as subscription
    """.format(ml=email, fl=link, usertitle=usertitle, usertag=usertag)
    return s


@escaped
def get_user_feeds(email):
    cph = """\
    MATCH (:User {{ email: {email} }})-[:SUBSCRIBES]->(feed:Feed)
    RETURN feed
    """

    return cph.format(email=email)


@escaped
def get_users_new_feeditems(email, lastsync):
    cph = """\
    MATCH (:User {{ email: {email} }})-[s:SUBSCRIBES]->(feed:Feed)
    WHERE s.timestamp > {lastsync} OR feed.timestamp > {lastsync}
    WITH feed
    OPTIONAL MATCH (feed)<-[:IN]-(item:Item)
    WHERE item.timestamp > {lastsync}
    RETURN feed, COLLECT(item) as items
    """
    return cph.format(email=email, lastsync=lastsync)


@escaped
def get_feed_and_items(link, limit):
    cph = """\
    MATCH (feed:Feed {{link: {link}}})
    WITH feed
    OPTIONAL MATCH (feed)<-[:IN]-(item:Item)
    WITH feed, item
    ORDER BY item.published DESC
    LIMIT {limit}
    RETURN feed, COLLECT(item) as items
    """
    return cph.format(link=link, limit=limit)


@escaped
def get_users_new_unsubscribes(email, lastsync):
    cph = """\
MATCH (:User {{ email: {email} }})-[s:UNSUBSCRIBED]->(feed:Feed)
WHERE s.timestamp > {lastsync}
RETURN feed"""
    return cph.format(email=email, lastsync=lastsync)


def get_subscribed_feeds():
    cph = """\
    MATCH (:User)-[:SUBSCRIBES]->(feed:Feed)
    RETURN DISTINCT feed
    """
    return cph


def on_synced(feed, ts, items):
    '''
    Feed will always exist.
    Items might exist, and they require guid to be present.
    '''
    feed = escapedict(feed)
    items = [escapedict(i) for i in items]

    s = """\
MERGE (f:Feed {{link: {link} }})
ON MATCH SET f.timestamp = {ts}, f.title = {title}, \
f.description = {description}, f.published = {published}, f.etag = {etag}, \
f.modified = {modified}\
""".format(ts=ts, link=feed['link'], title=feed['title'],
           description=feed['description'],
           published=feed['published'],
           etag=feed['etag'], modified=feed['modified'])

    # Every item might exist, but always update
    for i in items:
        s += """
WITH f
MERGE (f)<-[:IN]-(i:Item {{ guid: {guid} }} )
SET i.link = {link}, i.title = {title}, i.description = {description}, \
i.title_stripped = {title_stripped}, i.snippet = {snippet}, \
i.timestamp = {ts}, i.published = {published}, i.author = {author}, \
i.comments = {comments}, i.enclosure = {enclosure}, i.image = {image}
""".format(ts=ts,
           guid=i['guid'],
           link=i['link'],
           title=i['title'],
           description=i['description'],
           title_stripped=i['title_stripped'],
           snippet=i['snippet'],
           published=i['published'],
           author=i['author'],
           comments=i['comments'],
           enclosure=i['enclosure'],
           image=i['image'])

    return s


@escaped
def cleanup_items(link):
    # No need to keep very old items, so clear them out
    # I want to remove items which are older than 2 weeks
    # IF they are not part of the newest X items
    cph = """\
MATCH (f:Feed {{ link: {link} }})--(i:Item)
WITH i
ORDER BY i.timestamp DESC
SKIP 100

MATCH (i)
WHERE i.timestamp < (timestamp() - 1209600000)
OPTIONAL MATCH (i)-[r]-()
DELETE i, r
RETURN COUNT(i) as deleted
"""
    return cph.format(link=link)
