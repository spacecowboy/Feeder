# -*- coding: utf-8 -*-
'''
The database models (tables)
'''

from sqlalchemy import (Column, Integer, String, Text,
                        DateTime, ForeignKey)
from sqlalchemy.orm import relationship, backref
from sqlalchemy.ext.associationproxy import association_proxy
from feeder.database import db


class User(db.Model):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    email = Column(String, nullable=False, unique=True)
    feeds = relationship('UserFeed', lazy='dynamic',
                         cascade='all, delete-orphan',
                         backref=backref('user'))

    def __init__(self, email):
        self.email = email

    def __repr__(self):
        return '<User {}>'.format(self.email)


class Feed(db.Model):
    __tablename__ = 'feeds'
    id = Column(Integer, primary_key=True)
    link = Column(String, nullable=False, unique=True)
    title = Column(Text, nullable=False)
    description = Column(Text, nullable=False)
    timestamp = Column(DateTime, nullable=False)
    published = Column(DateTime)
    # For feedparser's benefit
    etag = Column(String)
    modified = Column(String)

    def __init__(self, title, description, link, timestamp,
                 published=None, etag=None, modified=None):
        self.title = title
        self.description = description
        self.link = link
        self.timestamp = timestamp
        self.published = published
        self.etag = etag
        self.modified = modified

    def __repr__(self):
        return '<Feed {}>'.format(self.link)


# Tie Users and Feeds together with a Helper table
class UserFeed(db.Model):
    __tablename__ = "userfeeds"
    user_id = Column(Integer, ForeignKey('users.id'), primary_key=True)
    feed_id = Column(Integer, ForeignKey('feeds.id'), primary_key=True)
    tag = Column(String)
    title = Column(String, nullable=False)
    feed = relationship(Feed, lazy='joined')
    # Want to be able to access elements easily
    link = association_proxy('feed', 'link')
    description = association_proxy('feed', 'description')
    timestamp = association_proxy('feed', 'timestamp')
    published = association_proxy('feed', 'published')

    def __init__(self, user, feed, tag=None, title=None):
        self.user_id = user.id
        self.feed_id = feed.id
        # User can specify a tag
        self.tag = tag
        # User can specify his own title
        self.title = title or feed.title

    def __repr__(self):
        return '<Feed {}>'.format(self.link)


class FeedItem(db.Model):
    __tablename__ = "feeditems"
    id = Column(Integer, primary_key=True)
    link = Column(String, nullable=False, unique=True)
    title = Column(Text, nullable=False)
    description = Column(Text, nullable=False)
    title_stripped = Column(Text, nullable=False)
    snippet = Column(Text, nullable=False)
    published = Column(DateTime)
    author = Column(String)
    comments = Column(String)
    enclosure = Column(String)
    # Internal use
    timestamp = Column(DateTime, nullable=False)
    # Related feed
    feed_id = Column(Integer, ForeignKey('feeds.id'))
    feed = relationship("Feed", backref=backref('items',
                                                order_by=published,
                                                lazy='dynamic'))

    def __init__(self, title, description, link, title_stripped,
                 snippet, feed_id, published, author, comments,
                 enclosure, timestamp):
        self.title = title
        self.description = description
        self.link = link
        self.title_stripped = title_stripped
        self.snippet = snippet
        self.feed_id = feed_id
        self.published = published
        self.author = author
        self.comments = comments
        self.enclosure = enclosure
        self.timestamp = timestamp

    def ___repr__(self):
        return '<FeedItem {}>'.format(self.link)
