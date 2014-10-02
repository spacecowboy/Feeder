# -*- coding: utf-8 -*-
'''
The REST-API of Feeder
'''

from feeder import app, db
from feeder.models import User, Feed, FeedItem, UserFeed
#from flask_oauthlib.client import OAuth
from flask.ext.restful import (Resource, Api, reqparse, fields,
                               marshal_with, marshal_with_field)
from feeder.util import parse_timestamp
from datetime import datetime


# Want a boolean class
class fieldbool(fields.Raw):
    def format(self, value):
        if value:
            return 'true'
        else:
            return 'false'

# Set up Auth
#oauth = OAuth(app)

#github = oauth.remote_app(
#    'github',
#    consumer_key='a11a1bda412d928fb39a',
#    consumer_secret='92b7cf30bc42c49d589a10372c3f9ff3bb310037',
#    request_token_params={'scope': 'user:email'},
#    base_url='https://api.github.com/',
#    request_token_url=None,
#    access_token_method='POST',
#    access_token_url='https://github.com/login/oauth/access_token',
#    authorize_url='https://github.com/login/oauth/authorize'
#)

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
    'published': fields.DateTime,
    'author': fields.String,
    'comments': fields.String,
    'enclosure': fields.String,
    'tags': fields.List(fields.String),
    'read': fieldbool(default=False)
}
### Single feed with a possible list of items
feed_fields = {
    'link': fields.String,
    'title': fields.String,
    'description': fields.String,
    'published': fields.DateTime,
    'tag': fields.String,
    'timestamp': fields.DateTime,
    'items': fields.List(fields.Nested(feeditem_fields))
}


def get_user(email):
    '''
    Return a valid database user, creating one first if necessary.
    '''
    # Try to find it first
    user = User.query.filter_by(email=email).first()

    if user is None:
        # Add to database first
        user = User(email=email)
        db.session.add(user)
        db.session.commit()

    return user


def add_feed(link):
    '''
    Add a feed to database if it does not exist yet.

    Returns a valid feed object
    '''
    feed = Feed.query.filter_by(link=link).first()

    if feed is None:
        feed = Feed(title='', description='', link=link,
                    timestamp=datetime.utcnow())
        db.session.add(feed)
        db.session.commit()

    return feed


class Feeds(Resource):
    '''
    This class is the entire REST-interface for dealing with feeds.
    '''

    @marshal_with_field(fields.List(fields.Nested(feed_fields)))
    def get(self):
        '''Return all feeds'''
        args = getparser.parse_args()

        # Placeholder until auth
        email = "jonas@kalderstam.se"

        user = get_user(email)

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
        # Set the items on the outer object
        for f in feeds:
            f.items = f.feed.items

        return feeds

    @marshal_with(feed_fields)
    def post(self):
        '''Add new feed'''
        # Placeholder until auth
        email = "jonas@kalderstam.se"

        user = get_user(email)

        args = postparser.parse_args()

        # Make sure feed exists
        feed = add_feed(args.link)
        # Set link between user and feed
        userfeed = UserFeed(user, feed, args.tag, args.title)
        db.session.add(userfeed)
        db.session.commit()

        userfeed.items = None
        # Return feed
        return userfeed

    def delete(self):
        '''Delete a feed'''
        # Placeholder until auth
        email = "jonas@kalderstam.se"
        user = get_user(email)

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
