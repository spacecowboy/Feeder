import webapp2

from google.appengine.api import taskqueue
from util import (datetime_now, parse_timestamp, domain_from_url)
from cleaner import get_feeditem_model
from models import (FeedModel, FeedItemModel,
                    FeedModelKey, FeedItemKey)
import feedparser as fp


class Cacher(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write('url: ' + self.request.get("url"))

    def post(self):
        print("In cache post")

        # If no urls specified, fetch from user's store
        etag, modified = None, None
        url = self.request.get("url")
        if url is None or len(url) == 0:
            print("No url was specified")
            urls = FeedModel.query(projection=["link", "etag", "modified"],
                                   distinct=True)
        else:
            print("Using " + url)
            urls = [url]

        # Fetch RSS items
        for url in urls:
            # Check if it is a feed or string
            if hasattr(url, "link"):
                # It's a feed
                exists = True
                etag = url.etag
                modified = url.modified
                url = url.link
                print("From DB: ", url)

            else:
                print("From request: ", url)
                # Make sure it exists in feeds list
                feeds = FeedModel.query(FeedModel.link == url)
                exists = False
                for feed in feeds:
                    exists = True
                    etag = feed.etag
                    modified = feed.modified
                    break

            # cache it if it exists in database
            if exists:
                print("Exists, caching..")
                self._cache_feed(url, etag, modified)

    def _cache_feed(self, url, etag=None, modified=None, tag=None):
            '''
            Caches a feed to the database.

            '''
            if not "://" in url:
                url = "http://" + url

            # Parse the result
            rss = fp.parse(url, etag=etag, modified=modified)
            if "etag" in rss:
                print("etag:", rss.etag)
            if "modified" in rss:
                print("modified:", rss.modified)
            f = rss.feed
            # If feed does not have title, which is a required attribute,
            # we skip it.
            if not hasattr(f, "title"):
                print(rss)
                print(rss.debug_message)
                return

            # Update feed first
            timestamp = datetime_now()
            feeds = FeedModel.query((FeedModel.link == url))
            for feed in feeds:
                # Update fields
                feed.timestamp = timestamp
                feed.description = f.get("description", "")
                feed.title = f.title
                feed.published = f.get("published", None)
                feed.etag = rss.get("etag", None)
                feed.modified = rss.get("modified", None)
                # Save
                print("Caching:", feed.title)
                # TODO use put_multi or such
                feed.put()

            # Get individual items
            for item in rss.entries:
                feeditem = get_feeditem_model(url, timestamp, item)
                print("Got and item and putting it:", item.title)
                feeditem.put()


application = webapp2.WSGIApplication([('/tasks/cache', Cacher)], debug=True)
