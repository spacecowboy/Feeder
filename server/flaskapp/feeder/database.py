# -*- coding: utf-8 -*-
'''
The database
'''

from feeder import app
from flask.ext.sqlalchemy import SQLAlchemy


# Setup the database
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:////tmp/test.db'
db = SQLAlchemy(app)

# Now import models
import feeder.models
