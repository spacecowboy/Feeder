'''
Usage
    python program -c /path/to/config.yaml
'''
import sys
from feeder import read_config
from py2neo import Graph
import json


def presetup(graph):
    feed_unique = "CREATE CONSTRAINT ON (feed:Feed) ASSERT feed.link IS UNIQUE"
    #feed_index


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

if __name__ == "__main__":
    if len(sys.argv) != 3 or sys.argv[1] != '-c':
        exit(__doc__)

    graph = Graph('http://neo4j:neo@localhost:7474/db/data/')
    presetup(graph)

    configfile = sys.argv[2]
    read_config(configfile)

    from feeder.models import FeedItem, Feed, UserFeed

    for feed in Feed.query.all():
        migrate_feed(graph, feed)
