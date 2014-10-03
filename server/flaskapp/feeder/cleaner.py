# -*- coding: utf-8 -*-
""" Contains methods relevant for cleaning up RSS feeds."""

from .util import parse_timestamp
import re

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


def clean_entry(feeditem, item):
    """
    Sets cleaned versions of item's attributes on feeditem

    Parameters:

    feeditem - A database object
    item - A feedparser rss-entry
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

    # Set attributes
    feeditem.title = item.title
    feeditem.description = clean_description
    feeditem.link = item.link
    feeditem.title_stripped = get_snippet(item.title)
    feeditem.snippet = get_snippet(clean_description)
    feeditem.published = parse_timestamp(item.get("published", None))
    feeditem.author = item.get("author", None)
    feeditem.comments = item.get("comments", None)

    e = [e.href for e in item.get("enclosures", [])]
    if len(e) > 0:
        feeditem.enclosure = e[0]


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


if __name__ == "__main__":
    import doctest
    doctest.testmod()
