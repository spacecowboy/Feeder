# -*- coding: utf-8 -*-
'''
The REST-API of Feeder
'''

from flask import Flask
from flask.ext.restful import (Resource, Api, reqparse, fields,
                               marshal_with, marshal_with_field)


# Want a boolean class
class fieldbool(fields.Raw):
    def format(self, value):
        if value:
            return 'true'
        else:
            return 'false'


# Set up the web app
app = Flask(__name__)
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


class Feeds(Resource):
    '''
    This class is the entire REST-interface for dealing with feeds.
    '''

    @marshal_with_field(fields.List(fields.Nested(feed_fields)))
    def get(self):
        '''Return all feeds'''
        args = getparser.parse_args()

        # test
        urls = []
        if args['url'] is not None:
            urls = [u for u in args['url']]

        feed = dict(link="google.com", title="bob",
                    description=args['min_timestamp'],
                    items=[dict(link=u) for u in urls])
        return [feed]

    @marshal_with(feed_fields)
    def post(self):
        '''Add new feed'''
        args = postparser.parse_args()
        # test
        print("Feed to add:", args.link)
        return dict(title='yay', link=args.link, description='woo', items=None)

    def delete(self):
        '''Delete a feed'''
        args = deleteparser.parse_args()
        # test
        print("Deleting", args.link)
        return None, 204

# Connect with API URLs
api.add_resource(Feeds, '/feeds', '/feeds/<string:link>')


if __name__ == '__main__':
    app.run(debug=True)
