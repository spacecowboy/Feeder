# -*- coding: utf-8 -*-
'''
The REST-API of Feeder
'''

from feeder import app, db
from .models import Feed, FeedItem, UserFeed, get_user, get_feed, get_userfeed
#from flask_oauthlib.client import OAuth
from flask.ext.restful import (Resource, Api, reqparse, fields,
                               marshal_with, marshal_with_field)
from .util import parse_timestamp, datetime_to_string
from .gauth import authorized


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
    'timestamp': FieldDateTime,
    'items': fields.List(fields.Nested(feeditem_fields))
}


class Feeds(Resource):
    '''
    This class is the entire REST-interface for dealing with feeds.
    '''

    @marshal_with_field(fields.List(fields.Nested(feed_fields)))
    @authorized
    def get(self, userid):
        '''Return all feeds'''
        args = getparser.parse_args()

        user = get_user(userid)

        #Wrong
        # Query for feeds using lazy relationship
        q = user.feeds
        # Filters
        if args['link'] is not None:
            urls = [u for u in args['link']]
            q = q.filter(Feed.link.in_(urls))
        if args['min_timestamp'] is not None:
            dt = parse_timestamp(args['min_timestamp'])
            if dt is not None:
                q = q.filter(Feed.timestamp > dt)

        feeds = q.all()
        for f in feeds:
            # Make sure to only return items with correct timestamp
            # Set the items on the outer object
            f.items = FeedItem.query.filter(FeedItem.timestamp > dt,
                                            FeedItem.feed_id == f.id).all()
            f.items = f.feed.items

        return feeds

    @marshal_with(feed_fields)
    @authorized
    def post(self, userid):
        '''Add new/Edit feed'''
        user = get_user(userid)

        args = postparser.parse_args()

        # Make sure feed exists
        feed = get_feed(args.link)
        # Set link between user and feed
        userfeed = get_userfeed(user, feed, args.tag, args.title)

        # If we should update tag or title
        if userfeed.tag != args.tag or userfeed.title != args.title:
            userfeed.tag = args.tag
            userfeed.title = args.title
            db.session.add(userfeed)
            db.session.commit()
        # Else, already saved

        userfeed.items = None
        # Return feed
        return userfeed

    @authorized
    def delete(self, userid):
        '''Delete a feed'''
        user = get_user(userid)

        args = deleteparser.parse_args()

        feed = Feed.query.filter_by(link=args.link).first()

        if feed is None:
            return None, 404

        UserFeed.query.\
            filter_by(user_id=user.id).\
            filter_by(feed_id=feed.id).\
            delete()

        return None, 204

# Connect with API URLs
api.add_resource(Feeds, '/feeds', '/feeds/<string:link>')
