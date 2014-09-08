import webapp2

from google.appengine.api import taskqueue
from util import (datetime_now, parse_timestamp, domain_from_url,
                  datetuple_to_string)
from cleaner import get_feeditem_model
from models import (FeedModel, FeedItemModel,
                    FeedModelKey, FeedItemKey)
import feedparser as fp

# Don't remove embedded videos
fp._HTMLSanitizer.acceptable_elements = \
    set(list(fp._HTMLSanitizer.acceptable_elements) + ['object',
                                                       'embed',
                                                       'iframe'])


class Cacher(webapp2.RequestHandler):
    def printpage(self, msg):
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write(str(msg))

    def get(self):
        """Get is called by cron, which caches all feeds."""
        feeds = self._fetch_all_feeds()

        if feeds is None:
            print("No feeds")
            self.printpage("No feeds")
            return

        for feed in feeds:
            self._cache_feed(feed.link, feed.etag, feed.modified)

        self.printpage("Caching done.")

    def post(self):
        """Post is used when new feeds are added."""
        print("In cache post")

        # If no urls specified, fetch all
        etag, modified = None, None
        url = self.request.get("url")
        if url is None or len(url) == 0:
            urls = self._fetch_all_feeds()
        else:
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
            else:
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
                self._cache_feed(url, etag, modified)

    def _fetch_all_feeds(self):
        # If no urls specified, fetch all
        return FeedModel.query(projection=["link", "etag", "modified"],
                               distinct=True)

    def _cache_feed(self, url, etag=None, modified=None, tag=None):
            '''
            Caches a feed to the database.

            url can be either a string or a feedmodel.

            '''
            if not "://" in url:
                url = "http://" + url

            # Parse the result
            rss = fp.parse(url, etag=etag, modified=modified)
            #if "etag" in rss:
            #    print("etag:", rss.etag)
            #if "modified" in rss:
            #    print("modified:", rss.modified)
            f = rss.feed
            # If feed does not have title, which is a required attribute,
            # we skip it.
            if not hasattr(f, "title"):
                try:
                    print(rss.debug_message)
                except:
                    pass
                return

            # If no items, move on
            if len(rss.entries) == 0:
                print("No new items, ignoring {}".format(f.title))
                return

            # Update feed last
            timestamp = datetime_now()

            # Get individual items
            any_new_items = False
            for item in rss.entries:
                feeditem = get_feeditem_model(url, timestamp, item)
                # Ignores existing items
                if feeditem is not None:
                    any_new_items = True
                    feeditem.put()
                #else:
                #    print("Ignoring existing feeditem")

            # Only update feed if any new items were retrieved
            if any_new_items:
                feeds = FeedModel.query((FeedModel.link == url))
                for feed in feeds:
                    # Update fields
                    feed.timestamp = timestamp
                    feed.description = f.get("description", "")
                    # Don't override user's own title
                    #feed.title = f.title
                    feed.published = datetuple_to_string(f.get("published_parsed", None))
                    feed.etag = rss.get("etag", None)
                    feed.modified = rss.get("modified", None)
                    # Save
                    print("Cached:", feed.title)
                    # TODO use put_multi or such
                    feed.put()



application = webapp2.WSGIApplication([('/tasks/cache', Cacher)], debug=True)
