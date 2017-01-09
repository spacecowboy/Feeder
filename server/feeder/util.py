from datetime import datetime, timezone


def datetuple_to_string(tup):
    '''
    Convert a Python date tuple, as returned by feedparser's published_parsed,
    to a ISO date string. If None, returns None.

    Example:
    >>> datetuple_to_string(None)

    >>> datetuple_to_string((2009, 3, 23, 13, 6, 34, 0, 82, 0))
    '2009-03-23T13:06:34'
    '''
    if tup is None:
        return None
    return datetime_to_string(datetime(*tup[:6]))


def datetuple_to_datetime(tup):
    '''
    Convert a Python date tuple, as returned by feedparser's published_parsed,
    to a datetime. If None, returns None.

    Example:
    >>> datetuple_to_datetime(None)

    >>> datetuple_to_datetime((2009, 3, 23, 13, 6, 34, 0, 82, 0))
    datetime.datetime(2009, 3, 23, 13, 6, 34)
    '''
    if tup is None:
        return None
    return parse_timestamp(datetuple_to_string(tup))


def datetime_to_string(dt):
    '''Converts a datetime object to a
    timestamp string in the format (in UTC):

    2013-09-23T23:23:12.123456'''
    if dt is None:
        return dt

    if dt.utcoffset() is not None:
        dt = dt.astimezone(timezone.utc)

    return dt.isoformat()


def datetime_now():
    '''Return a datetime with current time
    in UTC timezone.'''
    return datetime.utcnow()


def datetime_string_now():
    '''Return current datetime as a string'''
    return datetime_to_string(datetime_now())


def convert_timestamp(timestamp):
    '''Convert timestamp to ISO8601 in UTC

    Example:

    Note that named timezones are not supported
    >>> convert_timestamp("Fri, 05 Sep 2014 12:55:00 +0000")
    '2014-09-05T12:55:00+00:00'

    >>> convert_timestamp("Fri, 26 Sep 2014 04:00:00 -0000")
    '2014-09-26T04:00:00+00:00'

    >>> convert_timestamp("Fri, 05 Sep 2014 12:55:00 +0200")
    '2014-09-05T10:55:00+00:00'

    >>> convert_timestamp("Fri, 05 Sep 2014 12:55:00 GMT")
    '2014-09-05T12:55:00'

    >>> convert_timestamp("Fri, 05 Sep 2014 12:55:00 CET")
    '2014-09-05T12:55:00'
    '''
    dt = parse_timestamp(timestamp)
    if dt is None:
        # Failed, return original
        #print("Conversion failed: " + timestamp)
        return timestamp

    return datetime_to_string(dt)


def parse_timestamp(timestamp):
    '''Parses a timestamp string.
    Supports several formats, examples:

    In second precision, format common in RSS feeds
    >>> parse_timestamp("2013-09-29T13:21:42")
    datetime.datetime(2013, 9, 29, 13, 21, 42)

    Or in fractional second precision (shown in microseconds)
    >>> parse_timestamp("2013-09-29T13:21:42.123456")
    datetime.datetime(2013, 9, 29, 13, 21, 42, 123456)

    With timezone specified
    >>> parse_timestamp("2013-09-29T13:21:42Z")
    datetime.datetime(2013, 9, 29, 13, 21, 42)

    >>> parse_timestamp("2013-09-29T13:21:42+0200")
    datetime.datetime(2013, 9, 29, 11, 21, 42, tzinfo=datetime.timezone.utc)


    Formats common in Atom feeds
    >>> parse_timestamp("Fri, 05 Sep 2014 12:55:00 +0200")
    datetime.datetime(2014, 9, 5, 10, 55, tzinfo=datetime.timezone.utc)

    >>> parse_timestamp("Fri, 23 May 2014 10:56:50 GMT")
    datetime.datetime(2014, 5, 23, 10, 56, 50)

    >>> parse_timestamp("Fri 23 May 2014 10:56:50 -0200")
    datetime.datetime(2014, 5, 23, 12, 56, 50, tzinfo=datetime.timezone.utc)


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
            if result.utcoffset() is not None:
                result = result.astimezone(timezone.utc)
        except ValueError as e:
            result = None
            #print("Conversion error: ", e)

    return result


def domain_from_url(url):
    # Strip http://
    start = url.find("://")
    if start > 0:
        start += 3
    else:
        start = 0
    # Strip /foo/bar/rss.xml
    end = url[start:].find("/")
    if end < 1:
        return url[start:]
    else:
        return url[start:][:end]

if __name__ == "__main__":
    import doctest
    doctest.testmod()
