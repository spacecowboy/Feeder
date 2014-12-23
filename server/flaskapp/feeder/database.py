# -*- coding: utf-8 -*-
'''
The database
'''

from feeder import app
from flask.ext.sqlalchemy import SQLAlchemy
from sqlalchemy.engine import Engine
from sqlalchemy import event


# Make sure foreign keys are enforced
@event.listens_for(Engine, "connect")
def set_sqlite_pragma(dbapi_connection, connection_record):
    cursor = dbapi_connection.cursor()
    cursor.execute("PRAGMA foreign_keys=ON")
    cursor.close()


# Setup the database
if 'SQLALCHEMY_DATABASE_URI' not in app.config:
    raise ValueError('No database configured')
else:
    print('Using database:', app.config['SQLALCHEMY_DATABASE_URI'])
    # app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:////tmp/test.db'
    db = SQLAlchemy(app)
