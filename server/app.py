import endpoints
from messages import (FeedsResponse, VoidMessage,
                      Feed, feed_from_model,
                      feeditem_from_model,
                      REQUEST_FEEDQUERY, REQUEST_PUTFEED,
                      REQUEST_DELFEED, REQUEST_CACHEFEEDS,
                      GCMRegId)

from google.appengine.api import taskqueue
from protorpc import remote

from storage import (get_distinct_feeds, get_feeditems, get_feed_with_items,
                     put_feed, delete_feeds, put_gcm)

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
            urls = get_distinct_feeds(user=current_user)
        else:
            urls = request.urls

        # Fetch RSS items
        for url in urls:
            try:
                if not "://" in url:
                    url = "http://" + url

                print("Fetching from db")
                feed = get_feed_with_items(url, request.min_timestamp)
                # Append to response
                if feed is not None:
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
        feed = put_feed(request.link, current_user, request.title,
                        request.description, request.tag)

        # Cache it
        # Add the task to the default queue.
        taskqueue.add(url='/tasks/cache', params={'url': request.link})

        # Notify through GCM
        #send_link(link, request.regid)

        return feed

    @endpoints.method(REQUEST_DELFEED, VoidMessage,
                      name='feeds.delete',
                      path='feeds/delete',
                      http_method='POST')
    def delete_feed(self, request):
        current_user = endpoints.get_current_user()
        if current_user is None:
            raise endpoints.UnauthorizedException('Invalid token.')

        # Delete RSS feeds
        delete_feeds(request.urls, current_user)

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

        put_gcm(request.regid, current_user)

        # Return nothing
        return VoidMessage()


if __name__ != "__main__":
    # Set the application for GAE
    application = endpoints.api_server([FeederApi],
                                       restricted=False)
