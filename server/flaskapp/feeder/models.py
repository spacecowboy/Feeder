# -*- coding: utf-8 -*-
'''
The database models (tables)
'''

from sqlalchemy import (Column, Integer, String, Text,
                        DateTime, ForeignKey, desc)
from sqlalchemy.orm import relationship, backref
from sqlalchemy.ext.associationproxy import association_proxy
from .database import db
from datetime import datetime


def get_user(email, allow_creation=True):
    '''
    Return a valid database user, creating one first if necessary.
    '''
    # Try to find it first
    user = User.query.filter_by(email=email).first()

    if user is None and allow_creation:
        # Add to database first
        user = User(email=email)
        db.session.add(user)
        db.session.commit()

    return user


def get_feed(link, indicate_new=False):
    '''
    Add a feed to database if it does not exist yet.

    Returns a valid feed object

    If indicate_new is true, then a tuple is returned instead:

    (feed, new)

    Where feed is a valid feed object and new is a boolean
    indicating if the feed was added or not.
    '''
    if "://" not in link:
        link = "http://" + link
    feed = Feed.query.filter_by(link=link).first()

    if feed is None:
        feed = Feed(title='', description='', link=link,
                    timestamp=datetime.utcnow())
        db.session.add(feed)
        db.session.commit()

        new = True
    else:
        new = False

    if indicate_new:
        return feed, new
    else:
        return feed


def get_userfeed(user, feed, tag=None, title=None):
    '''
    Return a valid userfeed item, adding it if it doesn't exist.
    '''
    userfeed = UserFeed.query.filter_by(user_id=user.id,
                                        feed_id=feed.id).first()

    if userfeed is None:
        userfeed = UserFeed(user, feed, tag, title)
        db.session.add(userfeed)
        db.session.commit()

    return userfeed


class User(db.Model):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    email = Column(String, nullable=False, unique=True, index=True)
    passwordhash = Column(String)
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
    link = Column(String, nullable=False, unique=True, index=True)
    title = Column(Text, nullable=False)
    description = Column(Text, nullable=False)
    timestamp = Column(DateTime, nullable=False, index=True)
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
    user_id = Column(Integer,
                     ForeignKey('users.id',
                                ondelete="CASCADE",
                                onupdate="CASCADE"),
                     primary_key=True)
    feed_id = Column(Integer,
                     ForeignKey('feeds.id',
                                ondelete="CASCADE",
                                onupdate="CASCADE"),
                     primary_key=True)
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
        return '<UserFeed {}>'.format(self.link)


# Keep track of deletions with a similar table
class UserDeletion(db.Model):
    __tablename__ = "userdeletions"
    id = Column(Integer, primary_key=True)
    user_id = Column(Integer,
                     ForeignKey('users.id',
                                ondelete="CASCADE",
                                onupdate="CASCADE"))
    link = Column(String, nullable=False)
    timestamp = Column(DateTime, nullable=False, index=True)
    # Would want unique constraint, but can't get
    # on conflict things

    def __init__(self, user, feed):
        self.user_id = user.id
        self.link = feed.link
        self.timestamp = datetime.utcnow()

    def __repr__(self):
        return '<UserDeletion {}>'.format(self.link)


class FeedItem(db.Model):
    __tablename__ = "feeditems"
    id = Column(Integer, primary_key=True)
    link = Column(String, nullable=False, index=True)
    title = Column(Text, nullable=False)
    description = Column(Text, nullable=False)
    title_stripped = Column(Text, nullable=False)
    snippet = Column(Text, nullable=False)
    published = Column(DateTime)
    author = Column(String)
    comments = Column(String)
    enclosure = Column(String)
    image = Column(String)
    json = Column(Text)
    # Internal use
    timestamp = Column(DateTime, nullable=False, index=True)
    # Related feed
    feed_id = Column(Integer, ForeignKey('feeds.id',
                                         ondelete="CASCADE",
                                         onupdate="CASCADE"))
    feed = relationship("Feed",
                        backref=backref('items',
                                        cascade="all,delete",
                                        order_by=desc(published),
                                        lazy='dynamic'))

    def __repr__(self):
        return '<FeedItem {}>'.format(self.link)
