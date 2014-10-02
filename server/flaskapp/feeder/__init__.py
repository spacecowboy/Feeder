# -*- coding: utf-8 -*-
'''
Feeder web app
'''

from flask import Flask


app = Flask(__name__)

# Import database
from feeder.database import db
# Import REST API
import feeder.rest
