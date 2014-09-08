""" Contains methods relevant for cleaning up RSS feeds."""

try:
    from models import FeedItemModel, FeedItemKey
except:
    # This is OK if running tests...
    pass

from util import convert_timestamp
import re

# Productive patterns
PATTERN_IMG_URL = re.compile(r"(&lt;|<)img.*?src=(\"|')(.*?)(\"|')",
                             re.IGNORECASE | re.MULTILINE)
# Bloat patterns
PATTERN_FEEDFLARE = re.compile(r"(<|&lt;)div class=('|\")feedflare('|\").*?/div(>|&gt;)",
                               re.IGNORECASE | re.MULTILINE)
PATTERN_FEEDSPORTAL = re.compile(r"(<|&lt;)a((?!/a).)*feedsportal.*?/a(>|gt;)",
                                 re.IGNORECASE | re.MULTILINE)
PATTERN_LINKED_ZEROIMAGES = \
    re.compile(r"(<|&lt;)a((?!/a).)*width=('|\")1('|\")((?!/a).)*/a(>|&gt;)",
               re.IGNORECASE | re.MULTILINE)
PATTERN_ZEROIMAGES = \
    re.compile(r"(<|&lt;)img((?!/((>|&gt;)|img)).)*width=\\?('|\")1\\?('|\").*?/(img)?(>|&gt;)",
               re.IGNORECASE | re.MULTILINE)
PATTERN_PARAGRAPH_NEWLINE = \
    re.compile(r"((<|&lt;)/?p/?(>|&gt;))(\s*(<|&lt;)/?br/?(>|&gt;))+",
               re.IGNORECASE | re.MULTILINE)
PATTERN_MULTIPLE_NEWLINES = \
    re.compile(r"(((<|&lt;)/?br/?(>|&gt;))\s*){2,}",
               re.IGNORECASE | re.MULTILINE)
PATTERN_EMPTY_PARAGRAPHS = \
    re.compile(r"(((<|&lt;)(p)(>|&gt;))\s*((<|&lt;)/p(>|&gt;))|(<|&lt;)p/(>|&gt;))",
               re.IGNORECASE | re.MULTILINE)
PATTERN_EMPTY_DIVS = \
    re.compile(r"<div[^>]*>\s*</div>",
               re.IGNORECASE | re.MULTILINE)


def get_feeditem_model(url, timestamp, item):
    """
    Returns a new feeditem model with cleaned attributes.
    Returns none if item already exists.

    Parameters:

    url - The feed's url
    item - A feedparser rss-entry
    """
    for feeditem in FeedItemModel.query(FeedItemModel.key == FeedItemKey(url, item.link)).iter(keys_only=True):
        # Existing feed, so ignore
        return None

    clean_description = strip_bloat(item.get("description", ""))
    return FeedItemModel(key=FeedItemKey(url, item.link),
                         title=item.title,
                         description=clean_description,
                         link=item.link,
                         title_stripped=get_snippet(item.title),
                         snippet=get_snippet(clean_description),
                         timestamp=timestamp,
                         feed_link=url,
                         images=get_images(clean_description),
                         published=convert_timestamp(item.get("published", None)),
                         author=item.get("author", None),
                         comments=item.get("comments", None),
                         enclosures=[e.href for e in item.get("enclosures", [])],
                         tags=[t.term for t in item.get("tags", [])])


def get_snippet(text, maxlen=120):
    """
    Returns a stripped version of text which will
    not exceed maxlen in length and which is unescaped

    Example:
    >>> get_snippet("I &amp; <i>you</i> are <i>very silly</i>.", maxlen=10)
    'I & you ar'
    """
    #return s[:maxlen-1] + "\u2026"
    return unescape(strip_tags(text))[:maxlen]


def unescape(text):
    """
    Unescapes HTML-escaped text.

    Examples:
    >>> unescape("&lt; &rt;")
    '< >'
    >>> unescape("&quot;")
    '"'
    >>> unescape("&apos;")
    "'"
    >>> unescape("&amp;")
    '&'
    """
    text = text.replace("&lt;", "<")
    text = text.replace("&rt;", ">")
    text = text.replace("&quot;", '"')
    text = text.replace("&apos;", "'")
    text = text.replace("&amp;", "&")
    return text


def get_images(text):
    """
    Find and return the first image url in the document.
    None if nothing could be found.

    Examples:
    >>> get_images("No image here")
    []

    >>> get_images("Here's one: <img src='url'/>")
    ['url']

    >>> get_images("one: <img src='url1'/>, two: <img width='10' src='url2' height='50'/>")
    ['url1', 'url2']
    """
    images = []
    for m in PATTERN_IMG_URL.finditer(text):
        images.append(m.group(3))

    return images


def strip_tags(text):
    """
    Strips all html formatting from a string.

    Example:
    >>> strip_tags("An <tag>example text</tag> with tag.")
    'An example text with tag.'
    """
    # Remove all tags
    text = re.sub(r"<[^>]*>", "", text)
    # This might have introduced extra spaces, reduce to one
    text = re.sub(r"\s+", " ", text)
    return text


def strip_bloat(text):
    """
    Removes bloat, such as 1-pixel images, feedflare,
    share shit, etc, from the string.

    Examples:

    Too many newlines
    >>> strip_bloat('<br/> <br/>')
    '<br/>'
    >>> strip_bloat('<br><br><br>')
    '<br/>'
    >>> strip_bloat('<p> <br>')
    '<p>'
    >>> strip_bloat('<p>   </p>')
    ''

    Feedflare
    >>> strip_bloat('<div class="feedflare">blabla</div>')
    ''

    Feedsportal links
    >>> strip_bloat('<a href="http://feedsportal.com">bla</a>')
    ''

    Zero size images
    >>> strip_bloat('<img width="1" src="bla"/>')
    ''
    >>> strip_bloat('<img src="bla" width="1">blaa</img>')
    ''
    >>> strip_bloat('<img height=\"1\" src=\"http://feeds.feedburner.com/~r/cornubot/~4/-BDe1lEL8ys\" width=\"1\" />')
    ''
    """
    text = PATTERN_FEEDFLARE.sub("", text)
    text = PATTERN_FEEDSPORTAL.sub("", text)
    text = PATTERN_LINKED_ZEROIMAGES.sub("", text)
    text = PATTERN_ZEROIMAGES.sub("", text)
    text = PATTERN_MULTIPLE_NEWLINES.sub("<br/>", text)
    text = PATTERN_PARAGRAPH_NEWLINE.sub("<p>", text)
    # Take these last
    text = PATTERN_EMPTY_PARAGRAPHS.sub("", text)
    text = PATTERN_EMPTY_DIVS.sub("", text)

    return text


if __name__ == "__main__":
    import doctest
    doctest.testmod()
