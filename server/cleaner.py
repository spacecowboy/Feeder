""" Contains methods relevant for cleaning up RSS feeds."""

from models import FeedItemModel, FeedItemKey


def get_feeditem_model(url, timestamp, item):
    """
    Returns a feeditem model with cleaned attributes.

    Parameters:

    url - The feed's url
    item - A feedparser rss-entry
    """
    clean_description = strip_bloat(item.get("description", ""))
    return FeedItemModel(key=FeedItemKey(url, item.link),
                         title=item.title,
                         description=clean_description,
                         link=item.link,
                         title_stripped=get_snippet(item.title),
                         snippet=get_snippet(clean_description),
                         timestamp=timestamp,
                         feed_link=url,
                         published=item.get("published", None),
                         author=item.get("author", None),
                         comments=item.get("comments", None),
                         enclosures=[e.href for e in item.get("enclosures", [])],
                         tags=[t.term for t in item.get("tags", [])])


def get_snippet(text, maxlen=120):
    """
    Returns a stripped version of text which will
    not exceed maxlen in length.
    """
    #return s[:maxlen-1] + "\u2026"
    return strip_tags(text)[:maxlen]


def strip_tags(text):
    """
    Strips all html formatting from a string.
    """
    # TODO
    return text


def strip_bloat(text):
    """
    Removes bloat, such as 1-pixel images, feedflare,
    share shit, etc, from the string.
    """
    # TODO
    return text
