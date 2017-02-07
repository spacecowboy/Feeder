# -*- coding: utf-8 -*-
'''
Feeder web app

Configuration file can be set with environment variable FEEDER_CONFIG
'''

from flask import Flask

app = Flask(__name__)


# Load defaults
#app.config.from_object(DefaultConfig)

# read_config
from feeder.read_config import read_config
from feeder.version import *
