from datetime import datetime


def datetime_to_string(dt):
    '''Converts a datetime object to a
    timestamp string in the format:

    2013-09-23T23:23:12.123456'''
    return dt.isoformat()


def datetime_now():
    '''Return a datetime with current time
    in UTC timezone.'''
    return datetime.utcnow()


def datetime_string_now():
    '''Return current datetime as a string'''
    return datetime_to_string(datetime_now())


def parse_timestamp(timestamp):
    '''Parses a timestamp string.
    Supports several formats, examples:

    In second precision, format common in RSS feeds
    >>> parse_timestamp("2013-09-29T13:21:42")
    datetime.datetime(2013, 9, 29, 13, 21, 42)

    Or in fractional second precision (shown in microseconds)
    >>> parse_timestamp("2013-09-29T13:21:42.123456")
    datetime.datetime(2013, 9, 29, 13, 21, 42, 123456)

    Formats common in Atom feeds
    >>> parse_timestamp("Fri, 05 Sep 2014 12:55:00 +0200")
    datetime.datetime(2014, 9, 5, 12, 55, tzinfo=datetime.timezone(datetime.timedelta(0, 7200)))

    >>> parse_timestamp("Fri, 23 May 2014 10:56:50 GMT")
    datetime.datetime(2014, 5, 23, 10, 56, 50)

    >>> parse_timestamp("Fri 23 May 2014 10:56:50 -0200")
    datetime.datetime(2014, 5, 23, 10, 56, 50, tzinfo=datetime.timezone(datetime.timedelta(-1, 79200)))


    Returns None on failure to parse
    >>> parse_timestamp("2013-09-22")
    '''
    result = None

    formats = ['%Y-%m-%dT%H:%M:%S.%f',
               '%Y-%m-%dT%H:%M:%S',
               '%a, %d %b %Y %H:%M:%S %z',
               '%a, %d %b %Y %H:%M:%S %Z',
               '%a %d %b %Y %H:%M:%S %z',
               '%a %d %b %Y %H:%M:%S %Z']

    for fmt in formats:
        if result is not None:
            break

        try:
            result = datetime.strptime(timestamp,
                                       fmt)
        except ValueError:
            result = None

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
