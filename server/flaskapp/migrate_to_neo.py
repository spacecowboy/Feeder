'''
Usage
    python program -c /path/to/config.yaml
'''
import sys
from feeder import read_config
from aggregator.database import get_database
from aggregator.util import timestamp, escapedict
from datetime import datetime
import json


def presetup(db):
    db.graph.cypher.execute("CREATE CONSTRAINT ON (feed:Feed) ASSERT feed.link IS UNIQUE")
    db.graph.cypher.execute("CREATE CONSTRAINT ON (user:User) ASSERT user.email IS UNIQUE")


def migrate_feeditems(db, feed):
    # Make feed
    ts = int(1000 * feed.timestamp.timestamp())

    df = dict(timestamp=ts,
             link=feed.link,
             title=feed.title,
             description=feed.description,
             published=int(1000 * feed.published.timestamp()) if feed.published else ts,
             etag=feed.etag,
             modified=feed.modified)

    items = []
    for item in feed.items:
        its = int(1000 * item.timestamp.timestamp())
        ips = int(1000 * item.published.timestamp()) if item.published else its
        guid = item.link

        di = dict(timestamp=its,
             guid=guid,
             link=item.link,
             title=item.title,
             description=item.description,
             title_stripped=item.title_stripped,
             snippet=item.snippet,
             published=ips,
             author=item.author,
             comments=item.comments,
             enclosure=item.enclosure,
             image=item.image)

        items.append(di)

    db.on_synced(df, ts, items)


def migrate_user(db, u):
    db.merge_user(u.email, u.passwordhash)


def migrate_userfeed(db, u, f):
    db.subscribe(u.email, f.link, f.title, f.tag)


def migrate_userdeletion(db, u, fl):
    db.unsubscribe(u.email, fl)


if __name__ == "__main__":
    if len(sys.argv) != 3 or sys.argv[1] != '-c':
        exit(__doc__)

    db = get_database('http://localhost:7575/db/data/')
    presetup(db)

    configfile = sys.argv[2]
    read_config(configfile)

    from feeder.models import FeedItem, Feed, UserFeed, User, UserDeletion

    for user in User.query.all():
        print("User:", user.email)
        migrate_user(db, user)

        for feed in user.feeds.all():
            print("UserFeed:", feed.title)
            migrate_userfeed(db, user, feed)
            print("FeedItems:", len(feed.feed.items.all()))
            feed = feed.feed
            migrate_feeditems(db, feed)


        for feed in UserDeletion.query.filter(UserDeletion.user_id==user.id).all():
            print("UserDel:", feed.link)
            migrate_userdeletion(db, user, feed.link)
