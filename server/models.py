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
    user = ndb.UserProperty(required=True)
    title = ndb.TextProperty(required=True)
    description = ndb.TextProperty(required=True)
    link = ndb.StringProperty(required=True)
    # Internal use
    timestamp = ndb.DateTimeProperty(required=True)
    # Optionals
    # tag in app
    tag = ndb.StringProperty()
    # pubdate
    published = ndb.StringProperty()
    # For requests
    etag = ndb.StringProperty()
    modified = ndb.StringProperty()


class FeedItemModel(ndb.Model):
    title = ndb.TextProperty(required=True)
    description = ndb.TextProperty(required=True)
    link = ndb.StringProperty(required=True)
    title_stripped = ndb.StringProperty(required=True)
    snippet = ndb.StringProperty(required=True)
    # Internal use
    timestamp = ndb.DateTimeProperty(required=True)
    # Related feed
    feed_link = ndb.StringProperty(required=True)
    # Optionals
    published = ndb.StringProperty()
    author = ndb.StringProperty()
    comments = ndb.StringProperty()
    enclosures = ndb.StringProperty(repeated=True)
    tags = ndb.StringProperty(repeated=True)
