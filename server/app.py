import feedparser as fp

import endpoints
from messages import (FeedsResponse, VoidMessage,
                      Feed, feed_from_model,
                      feeditem_from_model,
                      REQUEST_FEEDQUERY, REQUEST_PUTFEED,
                      REQUEST_DELFEED, REQUEST_CACHEFEEDS,
                      GCMRegId)
from models import (FeedModel, FeedItemModel,
                    FeedModelKey, FeedItemKey,
                    GCMRegIdModel, GCMRegIdModelKey)
from util import (datetime_now, parse_timestamp, domain_from_url)
from cleaner import get_feeditem_model
#from google.appengine.ext import ndb

from google.appengine.api import taskqueue

from protorpc import remote

#from app_gcm import send_link, GCMRegIdModel


# Client id for webapps
CLIENT_ID = '175502335090-2adneckdaq5hg9mtv62thc0jsajpk6ui.apps.googleusercontent.com'
# Client id for devices (android apps)
CLIENT_ID_ANDROID = '175502335090-4q2k9s1msu87ekp9afq0qrlrrvab28nu.apps.googleusercontent.com'


@endpoints.api(name='feeder', version='v1',
               description='API for RSS/Atom fetching',
               allowed_client_ids=[CLIENT_ID, CLIENT_ID_ANDROID,
                                   endpoints.API_EXPLORER_CLIENT_ID]
               )
class FeederApi(remote.Service):
    '''This is the REST API. Annotations
    specify address, HTTP method and expected
    messages.'''

    @endpoints.method(REQUEST_FEEDQUERY,
                      FeedsResponse,
                      name='feeds.list',
                      path='feeds',
                      http_method='POST')
    def get_feeds(self, request):
        current_user = endpoints.get_current_user()
        if current_user is None:
            raise endpoints.UnauthorizedException('Invalid token.')

        # Response
        feeds = []

        print(dir(request))

        # If no urls specified, fetch from user's store
        if len(request.urls) == 0:
            urls = FeedModel.query(FeedModel.user == current_user)
        else:
            urls = request.urls

        # Fetch RSS items
        for url in urls:
            try:
                # Check if it is a feed or string
                if hasattr(url, "link"):
                    # It's a feed
                    print("Feed is already here")
                    fm = url
                    url = fm.link
                else:
                    if not "://" in url:
                        url = "http://" + url
                    # Try to look in cache
                    print("Looking in DB")
                    fm = FeedModelKey(current_user, url).get()
                print("DB result:", fm)
                if fm is None:
                    print("fm was none")
                    entries = None
                    # Not seen this before, cache it first
                    #fm, entries = self._cache_feed(url, current_user)
                    #print("cache result:", fm, entries)
                    if fm is None or entries is None:
                        # Nothing to do here
                        print("No cache result")
                        continue
                else:
                    # It is cached, fetch items
                    print("Fetching from db")
                    entries = FeedItemModel.query(FeedItemModel.feed_link == url)
                    entries.order(FeedItemModel.published)
                    # filter on timestamps
                    if (request.min_timestamp is not None and
                        parse_timestamp(request.min_timestamp) is not None):
                        entries = entries.filter(FeedItemModel.timestamp >\
                                                 parse_timestamp(request.min_timestamp))

                # Get individual items
                items = []
                for item in entries:
                    print("Iterating an item:", item.title)
                    items.append(feeditem_from_model(item))
                # Construct feed
                feed = feed_from_model(fm, items)

                # Append to response
                feeds.append(feed)
            except Exception as e:
                raise(e)

        # Build response
        return FeedsResponse(feeds=feeds)

    @endpoints.method(REQUEST_PUTFEED, Feed,
                      name='feeds.put',
                      path='feeds/put',
                      http_method='POST')
    def put_feed(self, request):
        current_user = endpoints.get_current_user()
        if current_user is None:
            raise endpoints.UnauthorizedException('Invalid token.')

        print("User:", current_user.email())

        if not "://" in request.link:
            request.link = "http://" + request.link

        print("Got a feed and putting it:", request.link)

        # Put in database
        feed = FeedModel(key=FeedModelKey(current_user, request.link),
                         user=current_user,
                         timestamp=datetime_now(),
                         title=request.title,
                         description=request.description,
                         link=request.link,
                         tag=request.tag)
        # Make sure feed has required items
        if feed.title is None:
            feed.title = domain_from_url(request.link)
        if feed.description is None:
            feed.description = ""
        # And save
        feed.put()

        # Cache it
        # Add the task to the default queue.
        taskqueue.add(url='/tasks/cache', params={'url': feed.link})

        # Notify through GCM
        #send_link(link, request.regid)

        return feed_from_model(feed)

    @endpoints.method(REQUEST_DELFEED, VoidMessage,
                      name='feeds.delete',
                      path='feeds/delete',
                      http_method='POST')
    def delete_feed(self, request):
        current_user = endpoints.get_current_user()
        if current_user is None:
            raise endpoints.UnauthorizedException('Invalid token.')

        # Delete RSS feeds
        for url in request.urls:
            print("Deleting:", url)
            FeedModelKey(current_user, url).delete()

        # Notify through GCM
        #send_link(link, request.regid)

        return VoidMessage()

    @endpoints.method(GCMRegId, VoidMessage,
                      name='gcm.register',
                      path='registergcm',
                      http_method='POST')
    def register_gcm(self, request):
        current_user = endpoints.get_current_user()
        if current_user is None:
            raise endpoints.UnauthorizedException('Invalid token.')

        device = GCMRegIdModel(key=GCMRegIdModelKey(request.regid),
                               regid=request.regid,
                               userid=current_user)
        # And save it
        device.put()

        # Return nothing
        return VoidMessage()


if __name__ != "__main__":
    # Set the application for GAE
    application = endpoints.api_server([FeederApi],
                                       restricted=False)
