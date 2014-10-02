'''
This file wraps the storage.
'''

from models import (FeedModel, FeedItemModel,
                    FeedModelKey, FeedItemKey,
                    GCMRegIdModel, GCMRegIdModelKey)
from messages import (feed_from_model,
                      feeditem_from_model)
from util import (parse_timestamp, datetime_now, domain_from_url)


def get_feedurls(user=None, distinct=False):
    '''
    Return a user's distinct feeds. If no user is specified,
    returns all distinct feeds.
    '''
    if user is None:
        feeds = FeedModel.query(projection=["link", "etag", "modified"],
                                distinct=distinct)
    else:
        feeds = FeedModel.query(FeedModel.user == user)

    return [f.link for f in feeds]


def get_feed(url):
    result = None
    feeds = FeedModel.query(FeedModel.link == url)
    for feed in feeds:
        result = feed
        break

    return result


def get_feeditems(url, min_timestamp=None):
    entries = FeedItemModel.query(FeedItemModel.feed_link == url)
    entries.order(FeedItemModel.published)
    # filter on timestamps
    if (min_timestamp is not None and
        parse_timestamp(min_timestamp) is not None):
        entries = entries.filter(FeedItemModel.timestamp >\
                                parse_timestamp(min_timestamp))

    # Get individual items
    items = []
    for item in entries:
        print("Iterating an item:", item.title)
        items.append(feeditem_from_model(item))

    return items


def get_feed_with_items(url, min_timestamp=None):
    feed = get_feed(url)
    if feed is None:
        return None

    items = get_feeditems(url, min_timestamp)
    return feed_from_model(feed, items)


def put_feed(url, user, title=None, description=None, tag=None):
    if title is None:
        title = domain_from_url(url)
    if description is None:
        description = ""

    feed = FeedModel(key=FeedModelKey(user, url),
                     user=user,
                     timestamp=datetime_now(),
                     title=title,
                     description=description,
                     link=url,
                     tag=tag)

    feed.put()

    return feed_from_model(feed)


def delete_feeds(urls, user):
    for url in urls:
        FeedModelKey(user, url).delete()


def put_gcm(regid, user):
    device = GCMRegIdModel(key=GCMRegIdModelKey(regid),
                               regid=regid,
                               userid=user)
    # And save it
    device.put()
