# -*- coding: utf-8 -*-
'''
Feeder web app

Configuration file can be set with environment variable FEEDER_CONFIG
'''

from flask import Flask

app = Flask(__name__)


# Set up some default values
class DefaultConfig(object):
    DEBUG = False
    SQLALCHEMY_DATABASE_URI = 'sqlite:///./feeder.db'
    FEEDER_ALLOW_GOOGLE = True
    FEEDER_ALLOW_USERPASS = True
    FEEDER_ALLOW_ANY_GOOGLE = False

# Load defaults
app.config.from_object(DefaultConfig)

# Import REST API
import feeder.rest
# read_config
from feeder.read_config import read_config
