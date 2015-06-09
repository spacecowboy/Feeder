# -*- coding: utf-8 -*-


def make_item(timestamp, guid, link, title, description, title_stripped,
              snippet, published, author=None, comments=None,
              enclosure=None, image=None):
    return dict(timestamp=timestamp, guid=guid, link=link, title=title,
                description=description, title_stripped=title_stripped,
                snippet=snippet, published=published, author=author,
                comments=comments, enclosure=enclosure, image=image)


def make_feed(timestamp, link, title, description, published,
              etag=None, modified=None):
    return dict(timestamp=timestamp, link=link, title=title,
                description=description, published=published,
                etag=etag, modified=modified)
