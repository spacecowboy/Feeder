# -*- coding: utf-8 -*-
'''
The REST-API of Feeder
'''

from aggregator import app
from flask.ext.restful import (Resource, Api, reqparse, fields,
                               marshal_with)
from .util import (parse_timestamp, timestamp, format_timestamp,
                   feed_to_dict, feeditem_to_dict)
from .sync import cache_feed
from .gauth import authorized
from .database import get_database

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

        return format_timestamp(value)

# Fetch the database
db = get_database()

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

## Deleting a feed
deleteparser = reqparse.RequestParser()
deleteparser.add_argument('link', type=str, required=True,
                          help='URL of the feed to delete')

# Set up return value mashers
## Get
### Single feed item
feeditem_fields = {
    'guid': fields.String,
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
    'read': FieldBool(default=False),
    'json': fields.String
}
### Single feed with a possible list of items
feed_fields = {
    'link': fields.String,
    'title': fields.String,
    'description': fields.String,
    'published': FieldDateTime,
    'tag': fields.String,
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

        # Timestamp is optional
        ts = None
        if args['min_timestamp'] is not None:
            ts = parse_timestamp(args['min_timestamp'])

        # Fetch new items
        res = db.get_users_new_feeditems(userid, ts)

        feeds = []
        for r in res:
            feed = r['feed']
            # Set items on feed for json conversion
            feed['items'] = r['items']
            # Add to list
            feeds.append(feed)

        # Fetch unsubscriptions if we have a timestamp
        deletes = []
        if ts is not None:
            for r in db.get_users_new_unsubscribes(userid, ts):
                deletes.append(r['feed'])

        return {"feeds": feeds, "deletes": deletes}

    @log_errors
    @marshal_with(feed_fields)
    @authorized
    def post(self, userid):
        '''Add new/Edit feed'''
        args = postparser.parse_args()

        feed, sub = db.subscribe(userid, args.link,
                                 args.title, args.tag)
        cache_feed(db, feed)

        feed, items = db.get_feed_and_items(args.link, limit=10)

        feed = feed_to_dict(feed)

        # Set on feed for JSON return
        # Change from node to dict...
        feed['items'] = [feeditem_to_dict(i) for i in items]

        # Set user title on feed
        if sub['usertitle']:
            feed['title'] = sub['usertitle']
        if sub['usertag']:
            feed['tag'] = sub['usertag']

        # Return feed
        return feed


class FeedsDeleter(Resource):
    @log_errors
    @authorized
    def post(self, userid):
        '''Delete a feed'''
        args = deleteparser.parse_args()

        db.unsubscribe(userid, args.link)

        return None, 204


class PingResponder(Resource):
    '''
    A method that allows the app to query if the server is alive.
    '''
    @log_errors
    def get(self):
        return {}, 200


# Connect with API URLs
api.add_resource(Feeds, '/feeds')
api.add_resource(FeedsDeleter, '/feeds/delete')
api.add_resource(PingResponder, '/ping')
