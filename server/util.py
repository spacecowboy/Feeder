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
    Supports two formats, examples:

    In second precision
    >>> parse_timestamp("2013-09-29T13:21:42")
    datetime.datetime(2013, 9, 29, 13, 21, 42)

    Or in fractional second precision (shown in microseconds)
    >>> parse_timestamp("2013-09-29T13:21:42.123456")
    datetime.datetime(2013, 9, 29, 13, 21, 42, 123456)

    Returns None on failure to parse
    >>> parse_timestamp("2013-09-22")
    '''
    result = None

    formats = ['%Y-%m-%dT%H:%M:%S.%f',
               '%Y-%m-%dT%H:%M:%S']

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
