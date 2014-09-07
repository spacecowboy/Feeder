import endpoints
from protorpc import messages
#from protorpc import message_types
from util import datetime_to_string, convert_timestamp

from protorpc.message_types import VoidMessage


class FeedsQuery(messages.Message):
    '''The post request body, contains a list of feed
    urls to fetch, and a min timestamp for caching.'''
    min_timestamp = messages.StringField(1)
    urls = messages.StringField(2, repeated=True)


class FeedItem(messages.Message):
    '''An RSS/Atom feed item.'''
    # Required elements
    title = messages.StringField(1, required=True)
    description = messages.StringField(2, required=True)
    link = messages.StringField(3, required=True)
    # Optional
    title_stripped = messages.StringField(4)
    snippet = messages.StringField(5)
    published = messages.StringField(6)
    author = messages.StringField(7)
    comments = messages.StringField(8)
    enclosures = messages.StringField(9, repeated=True)
    tags = messages.StringField(10, repeated=True)


def feeditem_from_model(item):
    return FeedItem(title=item.title,
                    description=item.description,
                    link=item.link,
                    title_stripped=item.title_stripped,
                    snippet=item.snippet,
                    published=convert_timestamp(item.published),
                    author=item.author,
                    comments=item.comments,
                    enclosures=item.enclosures,
                    tags=item.tags)


class Feed(messages.Message):
    '''An RSS/Atom feed. Contains a list of FeedItems.'''
    # Required elements
    link = messages.StringField(1, required=True)
    # Optional
    title = messages.StringField(2)
    description = messages.StringField(3)
    published = messages.StringField(4)
    tag = messages.StringField(5)
    timestamp = messages.StringField(6)
    # RSS items
    items = messages.MessageField(FeedItem, 7, repeated=True)


def feed_from_model(fm, items=None):
    if items is None:
        items = []
    return Feed(title=fm.title,
                description=fm.description,
                link=fm.link,
                tag=fm.tag,
                published=fm.published,
                timestamp=datetime_to_string(fm.timestamp),
                items=items)


class FeedsResponse(messages.Message):
    '''The post response, contains a list of feeds.'''
    feeds = messages.MessageField(Feed, 1, repeated=True)


# Add a device id to the user, database model in app_gcm.py
class GCMRegId(messages.Message):
    regid = messages.StringField(1, required=True)


REQUEST_FEEDQUERY = endpoints.ResourceContainer(
    FeedsQuery,
    regid=messages.StringField(2))

REQUEST_CACHEFEEDS = endpoints.ResourceContainer(
    FeedsQuery)

REQUEST_PUTFEED = endpoints.ResourceContainer(
    Feed,
    regid=messages.StringField(2))

REQUEST_DELFEED = endpoints.ResourceContainer(
    FeedsQuery,
    regid=messages.StringField(2))
