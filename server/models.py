from google.appengine.ext import ndb


def FeedModelKey(user, url):
    if not "://" in url:
        url = "http://" + url

    return ndb.Key(FeedModel, user.email(),
                   FeedModel, url)


def FeedItemKey(feed_url, item_link):
    if not "://" in feed_url:
        feed_url = "http://" + feed_url

    return ndb.Key(FeedModel, feed_url,
                   FeedItemModel, item_link)


class FeedModel(ndb.Model):
    user = ndb.UserProperty(required=True, indexed=True)
    title = ndb.TextProperty(required=True, indexed=False)
    description = ndb.TextProperty(required=True, indexed=False)
    link = ndb.StringProperty(required=True, indexed=True)
    # Internal use
    timestamp = ndb.DateTimeProperty(required=True, indexed=True)
    # Optionals
    # tag in app
    tag = ndb.StringProperty(indexed=False)
    # pubdate
    published = ndb.StringProperty(indexed=False)
    # For requests
    etag = ndb.StringProperty(indexed=True)
    modified = ndb.StringProperty(indexed=True)


class FeedItemModel(ndb.Model):
    title = ndb.TextProperty(required=True, indexed=False)
    description = ndb.TextProperty(required=True, indexed=False)
    link = ndb.StringProperty(required=True, indexed=True)
    title_stripped = ndb.StringProperty(required=True, indexed=False)
    snippet = ndb.StringProperty(required=True, indexed=False)
    # Internal use
    timestamp = ndb.DateTimeProperty(required=True, indexed=True)
    # Related feed
    feed_link = ndb.StringProperty(required=True, indexed=True)
    # Optionals
    images = ndb.StringProperty(repeated=True, indexed=False)
    published = ndb.StringProperty(indexed=False)
    author = ndb.StringProperty(indexed=False)
    comments = ndb.StringProperty(indexed=False)
    enclosures = ndb.StringProperty(repeated=True, indexed=False)
    tags = ndb.StringProperty(repeated=True, indexed=False)


# DeviceID in NDB
def GCMRegIdModelKey(regid):
    return ndb.Key(GCMRegIdModel, regid)


class GCMRegIdModel(ndb.Model):
    regid = ndb.StringProperty(required=True, indexed=False)
    userid = ndb.UserProperty(required=True, indexed=True)
