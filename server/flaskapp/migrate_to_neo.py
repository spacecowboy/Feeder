'''
Usage
    python program -c /path/to/config.yaml
'''
import sys
from feeder import read_config
from py2neo import Graph
from datetime import datetime
import json


def presetup(graph):
    graph.cypher.execute("CREATE CONSTRAINT ON (feed:Feed) ASSERT feed.link IS UNIQUE")
    #feed_index
    #graph.cypher.execute("CREATE CONSTRAINT ON (item:Item) ASSERT item.link IS UNIQUE")

    graph.cypher.execute("CREATE CONSTRAINT ON (user:User) ASSERT user.email IS UNIQUE")


def migrate_feeditem(graph, f, i):
    cph = """MERGE (i:Item {{ link: {link},
                              title: {title},
                              description: {description},
                              title_stripped: {title_stripped},
                              snippet: {snippet},
                              timestamp: {timestamp}
                              {nullables} }})"""

    nullables = ''

    if i.published is not None:
        nullables += ", published: {}".format(json.dumps(int(i.published.timestamp())))

    if i.author is not None:
        nullables += ", author: {}".format(json.dumps(i.author))

    if i.comments is not None:
        nullables += ", comments: {}".format(json.dumps(i.comments))

    if i.enclosure is not None:
        nullables += ", enclosure: {}".format(json.dumps(i.enclosure))

    if i.image is not None:
        nullables += ", image: {}".format(json.dumps(i.image))

    # Add for real database
    #if i.json is not None:
    #    nullables += ", json: {}".format(json.dumps(i.json))

    code = cph.format(link=json.dumps(i.link),
                      title=json.dumps(i.title),
                      description=json.dumps(i.description),
                      timestamp=json.dumps(int(i.timestamp.timestamp())),
                      title_stripped=json.dumps(i.title_stripped),
                      snippet=json.dumps(i.snippet),
                      nullables=nullables)

    graph.cypher.execute(code)

    # Relation
    rel = """MATCH (i:Item {{ link: {il} }}), (f:Feed {{link: {fl} }})
             CREATE (i)-[:IN]->(f)""".format(il=json.dumps(i.link),
                                             fl=json.dumps(f.link))
    graph.cypher.execute(rel)


def migrate_feed(graph, feed):
    cph = """MERGE (f:Feed {{ link: {link},
                               title: {title},
                               description: {description},
                               timestamp: {timestamp}
                               {nullables} }})"""

    nullables = ''

    if feed.published is not None:
        nullables += ', published: {}'.format(json.dumps(int(feed.published.timestamp())))

    if feed.etag is not None:
        nullables += ', etag: {}'.format(json.dumps(feed.etag))

    if feed.modified is not None:
        nullables += ', modified: {}'.format(json.dumps(feed.modified))


    code = cph.format(link=json.dumps(feed.link),
                      title=json.dumps(feed.title),
                      description=json.dumps(feed.description),
                      timestamp=json.dumps(int(feed.timestamp.timestamp())),
                      nullables=nullables)

    graph.cypher.execute(code)


def migrate_user(graph, u):
    cph = """MERGE (u:User {{ email: {email} {nullable} }})"""
    nullable = ''
    if u.passwordhash is not None:
       nullable = ',passwordhash: {pwhash}'.format(pwhash=json.dumps(u.passwordhash))

    graph.cypher.execute(cph.format(email=json.dumps(u.email), nullable=nullable))


def migrate_userfeed(graph, u, fl):
    # Relation
    rel = """MATCH (u:User {{ email: {email} }}), (f:Feed {{link: {fl} }})
             CREATE (u)-[:SUBSCRIBES {{ timestamp: {ts} }}]->(f)"""

    graph.cypher.execute(rel.format(email=json.dumps(u.email),
                    fl=json.dumps(fl),
                    ts=json.dumps(int(datetime.utcnow().timestamp()))))



def migrate_userdeletion(graph, u, fl):
    #Relation
    rel = """MATCH (u:User {{ email: {email} }}), (f:Feed {{link: {fl} }})
             CREATE (u)-[:UNSUBSCRIBED {{ timestamp: {ts} }}]->(f)"""

    graph.cypher.execute(rel.format(email=json.dumps(u.email),
                    fl=json.dumps(fl),
                    ts=json.dumps(int(datetime.utcnow().timestamp()))))



if __name__ == "__main__":
    if len(sys.argv) != 3 or sys.argv[1] != '-c':
        exit(__doc__)

    graph = Graph('http://neo4j:neo@localhost:7474/db/data/')
    presetup(graph)

    configfile = sys.argv[2]
    read_config(configfile)

    from feeder.models import FeedItem, Feed, UserFeed, User, UserDeletion

    #for i, feed in enumerate(Feed.query.all()):
        #print("{} Starting {}".format(i, feed.title))
        #migrate_feed(graph, feed)
        #for item in feed.items:
            #migrate_feeditem(graph, feed, item)


    for user in User.query.all():
        print("User")
        migrate_user(graph, user)

        for feed in user.feeds.all():
            print("UserFeed")
            migrate_userfeed(graph, user, feed.link)

        for feed in UserDeletion.query.filter(UserDeletion.user_id==user.id).all():
            print("UserDel")
            migrate_userdeletion(graph, user, feed.link)
