# -*- coding: utf-8 -*-
'''
The REST-API of Feeder
'''

from feeder import app, db
from .models import (Feed, FeedItem, UserFeed, UserDeletion,
                     get_user, get_feed, get_userfeed)
#from flask_oauthlib.client import OAuth
from flask import request
from flask.ext.restful import (Resource, Api, reqparse, fields,
                               marshal_with)
from .util import parse_timestamp, datetime_to_string
from .sync import cache_feed, get_fresh_feed

from .gauth import authorized

from datetime import datetime, timedelta

# Configure some logging
import logging
file_handler = logging.FileHandler('rest.log')
app.logger.addHandler(file_handler)
app.logger.setLevel(logging.INFO)


# Want a boolean class
class FieldBool(fields.Raw):
    def format(self, value):
        if value:
            return 'true'
        else:
            return 'false'


# Parse dates properly
class FieldDateTime(fields.Raw):
    def format(self, value):
        if value is None:
            return None

        return datetime_to_string(value)


# Set up the REST API
api = Api(app)

# Set up argument parsers
## Listing feeds
getparser = reqparse.RequestParser()
getparser.add_argument('min_timestamp', type=str, required=False,
                       help='Timestamp to filter on (only newer)')
getparser.add_argument('link', type=str, required=False, action='append',
                       help='Url(s) to limit query for')
## Adding feed
postparser = reqparse.RequestParser()
postparser.add_argument('link', type=str, required=True,
                        help='URL to the feed')
postparser.add_argument('title', type=str, required=False,
                        help='Title of feed')
postparser.add_argument('tag', type=str, required=False,
                        help='Tag to categorize feed under')

# Middle man parses feeds
middlemanparser = reqparse.RequestParser()
middlemanparser.add_argument('links', type=str, required=True, action='append',
                             help='RSS feed(s) to fetch.')
middlemanparser.add_argument('etag', type=str, required=False,
                             help='Etag of last sync')
middlemanparser.add_argument('modified', type=str, required=False,
                             help='Date of last sync')

## Deleting a feed
deleteparser = reqparse.RequestParser()
deleteparser.add_argument('link', type=str, required=True,
                          help='URL of the feed to delete')

# Set up return value mashers
## Get
### Single feed item
feeditem_fields = {
    'title': fields.String,
    'description': fields.String,
    'link': fields.String,
    'title_stripped': fields.String,
    'snippet': fields.String,
    'published': FieldDateTime,
    'author': fields.String,
    'comments': fields.String,
    'enclosure': fields.String,
    'tags': fields.List(fields.String),
    'image': fields.String,
    'read': FieldBool(default=False)
}
### Single feed with a possible list of items
feed_fields = {
    'link': fields.String,
    'title': fields.String,
    'description': fields.String,
    'published': FieldDateTime,
    'tag': fields.String,
    'etag': fields.String,
    'modified': fields.String,
    'timestamp': FieldDateTime,
    'items': fields.List(fields.Nested(feeditem_fields))
}
### Single delete
delete_fields = {
    'link': fields.String,
    'timestamp': FieldDateTime
}
### Response with list of feeds, and list of deletes
feeds_response = {
    'feeds': fields.List(fields.Nested(feed_fields)),
    'deletes': fields.List(fields.Nested(delete_fields))
}
# Response with list of feeds
middleman_response = {
    'feeds': fields.List(fields.Nested(feed_fields))
}


def log_errors(f):
    '''Log errors in the wrapped function and re-raise them.'''
    def wrapped_f(*args, **kwargs):
        try:
            return f(*args, **kwargs)
        except Exception as e:
            print(e)
            app.logger.error(str(e))
            raise e

    return wrapped_f


class Feeds(Resource):
    '''
    This class is the entire REST-interface for dealing with feeds.
    '''

    @log_errors
    @marshal_with(feeds_response)
    @authorized
    def get(self, userid):
        '''Return all feeds'''
        args = getparser.parse_args()

        user = get_user(userid)

        #Wrong
        # Query for feeds using lazy relationship
        q = user.feeds
        dt = None
        # Filters
        if args['link'] is not None:
            urls = [u for u in args['link']]
            q = q.filter(Feed.link.in_(urls))
        if args['min_timestamp'] is not None:
            dt = parse_timestamp(args['min_timestamp'])
        # Require a timestap. If one was not provided in decent form,
        # default to x days ago
        if dt is None:
            dt = datetime.utcnow() - timedelta(days=7)
        q = q.filter(Feed.timestamp > dt)
        feeds = q.all()
        for f in feeds:
            # Make sure to only return items with correct timestamp
            # Set the items on the outer object
            if dt is None:
                f.items = f.feed.items
            else:
                f.items = FeedItem.query.filter(FeedItem.timestamp > dt,
                                                FeedItem.feed_id == f.feed.id).all()

        # If we have a timestamp, also return deletes done
        if args['min_timestamp'] is None:
            deletes = []
        else:
            q = UserDeletion.query.filter(UserDeletion.timestamp > dt)
            deletes = q.all()

        return {"feeds": feeds, "deletes": deletes}

    @log_errors
    @marshal_with(feed_fields)
    @authorized
    def post(self, userid):
        '''Add new/Edit feed'''
        user = get_user(userid)

        args = postparser.parse_args()

        # Make sure feed exists
        feed, new = get_feed(args.link, indicate_new=True)
        if new:
            cache_feed(feed)

        # Set link between user and feed
        userfeed = get_userfeed(user, feed, args.tag, args.title)

        # Remove possible deletes
        UserDeletion.query.\
            filter_by(user_id=user.id).\
            filter_by(link=feed.link).\
            delete()

        # If we should update tag or title
        if userfeed.tag != args.tag or userfeed.title != args.title:
            userfeed.tag = args.tag
            userfeed.title = args.title
            db.session.add(userfeed)
        # Else, already saved
        db.session.commit()

        # TODO limit number of items instead of time
        # TODO include read information
        dt = datetime.utcnow() - timedelta(days=1)
        userfeed.items = FeedItem.query.filter(FeedItem.timestamp > dt,
                                               FeedItem.feed_id == feed.id)\
                                              .all()

        # Return feed
        return userfeed


class FeedsDeleter(Resource):
    @log_errors
    @authorized
    def post(self, userid):
        '''Delete a feed'''
        user = get_user(userid)

        args = deleteparser.parse_args()

        feed = Feed.query.filter_by(link=args.link).first()

        if feed is None:
            app.logger.error("No such feed: {}".format(args.link))
            return None, 404

        # Store delete for other devices
        ud = UserDeletion(user, feed)
        db.session.add(ud)

        # Perform delete
        UserFeed.query.\
            filter_by(user_id=user.id).\
            filter_by(feed_id=feed.id).\
            delete()

        db.session.commit()

        return None, 204


class PingResponder(Resource):
    '''
    A method that allows the app to query if the server is alive.
    '''
    @log_errors
    def get(self):
        return {}, 200


class MiddleMan(Resource):
    '''
    Talks with RSS servers on behalf of clients
    '''

    #@log_errors
    @marshal_with(middleman_response)
    def post(self):
        '''Return all feeds'''
        links = request.json['links']
        etag = request.json.get('etag', None)
        modified = request.json.get('modified', None)

        feeds = []
        for url in links:
            print("Url:", url)
            if "://" not in url:
                url = "http://" + url

            feed = get_fresh_feed(url, etag=etag, modified=modified)
            if feed is not None:
                feeds.append(feed)
            else:
                print("feed was None")

        return {"feeds": feeds}


# Connect with API URLs
api.add_resource(Feeds, '/feeds')
api.add_resource(FeedsDeleter, '/feeds/delete')
api.add_resource(PingResponder, '/ping')
api.add_resource(MiddleMan, '/middleman')
