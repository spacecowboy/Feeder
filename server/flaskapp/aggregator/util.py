# -*- coding: utf-8 -*-
from time import time
from datetime import datetime, timezone
import json


def unescapedict(*args, **kwargs):
    '''
    Reverse escaping done by escapedict.

    Example, a single dictionary object:
    >>> unescapedict({'a': '"a"'})
    {'a': 'a'}

    Example, keyword arguments:
    >>> unescapedict(a='"a"')
    {'a': 'a'}

    Example, several dictionaries
    >>> unescapedict({'a': '"a"'}, {'b': '"b"'})
    [{'a': 'a'}, {'b': 'b'}]

    Things that breaks json decoding are passed through
    >>> unescapedict(a='http://test.com')
    {'a': 'http://test.com'}
    '''
    if len(args) > 1:
        return [unescapedict(d) for d in args]
    elif len(args) == 1:
        kwargs = args[0]

    res = {}
    for k, v in kwargs.items():
        try:
            res[k] = json.loads(v)
        except:
            res[k] = v

    return res


def escapedict(*args, **kwargs):
    '''
    Push every item in a dictionary through json.dumps

    Example, a single dictionary object:
    >>> escapedict(dict(a='a'))
    {'a': '"a"'}

    Example, keyword arguments:
    >>> escapedict(a='a')
    {'a': '"a"'}

    Example, several dictionaries
    >>> escapedict(dict(a='a'), dict(b='b'))
    [{'a': '"a"'}, {'b': '"b"'}]
    '''
    if len(args) > 1:
        return [escapedict(d) for d in args]
    elif len(args) == 1:
        kwargs = args[0]

    return {k: json.dumps(v) for k, v in kwargs.items()}


def escaped(f):
    '''Annotation which pushes every argument through json.dumps'''

    def inner(*args, **kwargs):
        args = [json.dumps(x) for x in args]
        kwargs = {k: json.dumps(v) for k, v in kwargs.items()}
        return f(*args, **kwargs)

    return inner


def timestamp():
    '''
    timestamp() -> integer number

    Returns current time in milliseconds since the Epoch.
    '''
    return int(1000 * time())


def datetuple_to_timestamp(tup):
    '''
    Convert a Python date tuple, as returned by feedparser's published_parsed,
    to a timestamp (milliseconds since the epoch). If None, returns None.

    Example:
    >>> datetuple_to_timestamp(None)

    >>> datetuple_to_timestamp((2009, 3, 23, 12, 6, 34, 0, 82, 0))
    1237809994000
    '''
    if tup is None:
        return None

    return int(1000 * datetime(tzinfo=timezone.utc,
                               *tup[:6]).timestamp())


def parse_timestamp(timestamp):
    '''Parses a timestamp string and returns a timestamp in milliseconds
    since the epochs. Supports several formats, examples:

    In second precision, format common in RSS feeds
    >>> parse_timestamp("2013-09-29T13:21:42")
    1380460902000

    Or in fractional second precision (shown in microseconds)
    >>> parse_timestamp("2013-09-29T13:21:42.123456")
    1380460902123

    With timezone specified
    >>> parse_timestamp("2013-09-29T13:21:42Z")
    1380460902000

    >>> parse_timestamp("2013-09-29T13:21:42+0200")
    1380453702000

    Formats common in Atom feeds
    >>> parse_timestamp("Fri, 05 Sep 2014 12:55:00 +0200")
    1409914500000

    >>> parse_timestamp("Fri, 23 May 2014 10:56:50 GMT")
    1400842610000

    >>> parse_timestamp("Fri 23 May 2014 10:56:50 -0200")
    1400849810000


    Returns None on failure to parse
    >>> parse_timestamp("2013-09-22")

    >>> parse_timestamp(None)
    '''
    result = None

    if timestamp is None:
        return None

    formats = ['%Y-%m-%dT%H:%M:%S',
               '%Y-%m-%dT%H:%M:%SZ',
               '%Y-%m-%dT%H:%M:%S%z',
               '%Y-%m-%dT%H:%M:%S.%f',
               '%Y-%m-%dT%H:%M:%S.%fZ',
               '%Y-%m-%dT%H:%M:%S.%f%z',
               '%a, %d %b %Y %H:%M:%S',
               '%a %d %b %Y %H:%M:%S',
               '%a, %d %b %Y %H:%M:%S %z',
               '%a %d %b %Y %H:%M:%S %z',
               '%a, %d %b %Y %H:%M:%S %Z',
               '%a %d %b %Y %H:%M:%S %Z']

    for fmt in formats:
        if result is not None:
            break

        try:
            result = datetime.strptime(timestamp,
                                       fmt)
            if (result.tzinfo is not None
                and result.tzinfo.utcoffset(result) is not None):
                # If timezone is specified, convert to UTC
                result = result.astimezone(timezone.utc)
            else:
                # Otherwise assume UTC
                result = result.replace(tzinfo=timezone.utc)

            # Finally, convert to timestamp
            result = int(1000 * result.timestamp())
        except ValueError:
            result = None

    return result


def format_timestamp(ts):
    '''Converts a timestamp integer (milliseconds) to a
    timestamp string in the format (in UTC):

    2013-09-23T23:23:12.123456+00:00

    Examples:
    >>> format_timestamp(None)

    >>> format_timestamp(1237809994123)
    '2009-03-23T12:06:34.122999+00:00'
    '''
    if ts is None:
        return ts

    ts = int(ts) / 1000

    dt = datetime.utcfromtimestamp(ts)
    dt = dt.replace(tzinfo=timezone.utc)

    return dt.isoformat()


def feed_to_dict(feed):
    '''
    Convert a Node object to normal dict.
    Unescapes values.
    '''
    d = dict(timestamp=feed['timestamp'],
             link=feed['link'],
             title=feed['title'],
             description=feed['description'],
             published=feed['published'],
             etag=feed['etag'],
             modified=feed['modified'])

    return unescapedict(d)


def feeditem_to_dict(item):
    '''
    Convert a Node object to normal dict.
    Unescapes values.
    '''
    d = dict(timestamp=item['timestamp'],
             guid=item['guid'],
             link=item['link'],
             title=item['title'],
             description=item['description'],
             title_stripped=item['title_stripped'],
             snippet=item['snippet'],
             published=item['published'],
             author=item['author'],
             comments=item['comments'],
             enclosure=item['enclosure'],
             image=item['image'])

    return unescapedict(d)


if __name__ == "__main__":
    import doctest
    doctest.testmod()
