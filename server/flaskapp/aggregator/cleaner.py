# -*- coding: utf-8 -*-
from .util import parse_timestamp, datetuple_to_timestamp, escaped
import re
import html


# Productive patterns
PATTERN_IMG_URL = re.compile(r"(&lt;|<)img.*?src=(\"|')(.*?)(\"|')",
                             re.IGNORECASE | re.DOTALL)
# Bloat patterns
PATTERN_FEEDFLARE = re.compile(r"(<|&lt;)div\s+?class=('|\")feedflare('|\").*?/div(>|&gt;)",
                               re.IGNORECASE | re.DOTALL)
PATTERN_FEEDSPORTAL = re.compile(r"(<|&lt;)a((?!/a).)*feedsportal.*?/a(>|gt;)",
                                 re.IGNORECASE | re.DOTALL)
PATTERN_LINKED_ZEROIMAGES = \
    re.compile(r"(<|&lt;)a((?!/a).)*width=('|\")1('|\")((?!/a).)*/a(>|&gt;)",
               re.IGNORECASE | re.DOTALL)
PATTERN_ZEROIMAGES = \
    re.compile(r"(<|&lt;)img((?!/((>|&gt;)|img)).)*width=\\?('|\")1\\?('|\").*?/(img)?(>|&gt;)",
               re.IGNORECASE | re.DOTALL)
PATTERN_PARAGRAPH_NEWLINE = \
    re.compile(r"((<|&lt;)/?p/?(>|&gt;))(\s*(<|&lt;)/?br/?(>|&gt;))+",
               re.IGNORECASE | re.DOTALL)
PATTERN_MULTIPLE_NEWLINES = \
    re.compile(r"(((<|&lt;)/?br/?(>|&gt;))\s*){2,}",
               re.IGNORECASE | re.DOTALL)
PATTERN_EMPTY_PARAGRAPHS = \
    re.compile(r"(((<|&lt;)(p)(>|&gt;))\s*((<|&lt;)/p(>|&gt;))|(<|&lt;)p/(>|&gt;))",
               re.IGNORECASE | re.DOTALL)
PATTERN_EMPTY_DIVS = \
    re.compile(r"<div[^>]*>\s*</div>",
               re.IGNORECASE | re.DOTALL)


def get_clean_entry(item, timestamp):
    """
    Convert an RSS-Item from FeedParser into
    a dictionary suitable for insertion into the database.
    """
    # Some feeds use content, some description.
    # Take the longest.
    d = item.get("description", "")
    try:
        c = item.get("content", None)
        # Can be many content, with different types
        # This might fail for some
        c = c[0].value
    except:
        c = ""

    # Use d
    if len(c) > len(d):
        d = c

    clean_description = strip_bloat(d)

    e = [e.href for e in item.get("enclosures", [])]
    if len(e) > 0:
        enclosure = e[0]
    else:
        enclosure = None

    published = parse_timestamp(item.get("published", None))
    if published is None:
        published = timestamp

    guid = item.get("id", item.link)

    return make_item(timestamp=timestamp, guid=guid, link=item.link,
                     title=item.title, description=clean_description,
                     title_stripped=get_snippet(item.title),
                     snippet=get_snippet(clean_description),
                     published=published, author=item.get("author", None),
                     comments=item.get("comments", None),
                     enclosure=enclosure, image=get_image(clean_description),
                     json=item)


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
    >>> unescape("&lt; &gt;")
    '< >'
    >>> unescape("&quot;")
    '"'
    >>> unescape("&apos;")
    "'"
    >>> unescape("&#8217;")
    '’'
    >>> unescape("&amp;")
    '&'
    """
    text = html.unescape(text)
    return text


def get_image(text):
    '''
    Returns the first image referenced in the document.
    None if no images could be found.
    '''
    imgs = get_images(text)
    if imgs is None or len(imgs) == 0:
        return None
    else:
        return imgs[0]


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

    >>> get_images("Silly urls: <img src='bob&amp;jones'")
    ['bob&jones']
    """
    images = []
    for m in PATTERN_IMG_URL.finditer(text):
        # Do this somewhere else perhaps. Sweclockers has stupid image urls.
        images.append(m.group(3).replace("&amp;", "&"))

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
    >>> strip_bloat('<br/>\\n<br/>')
    '<br/>'
    >>> strip_bloat('<br>\\n<br>\\n<br>')
    '<br/>'
    >>> strip_bloat('<p>\\n<br>')
    '<p>'
    >>> strip_bloat('<p> \\n </p>')
    ''

    Feedflare
    >>> strip_bloat('<div class="feedflare">\\nblabla</div>')
    ''
    >>> strip_bloat("<div class='feedflare'>\\nblabla</div>")
    ''

    Feedsportal links
    >>> strip_bloat('<a href="http://feedsportal.com">\\nbla</a>')
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


@escaped
def make_item(timestamp, guid, link, title, description, title_stripped,
              snippet, published, author=None, comments=None,
              enclosure=None, image=None, json=None):
    """
    Easy way to get final result, if your arguments are well formed.
    """
    return dict(timestamp=timestamp, guid=guid, link=link, title=title,
                description=description, title_stripped=title_stripped,
                snippet=snippet, published=published, author=author,
                comments=comments, enclosure=enclosure, image=image,
                json=json)


@escaped
def make_feed(timestamp, link, title, description, published,
              etag=None, modified=None):
    """
    Easy way to get final result, if your arguments are well formed.
    """
    return dict(timestamp=timestamp, link=link, title=title,
                description=description, published=published,
                etag=etag, modified=modified)
