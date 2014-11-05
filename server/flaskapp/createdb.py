# -*- coding: utf-8 -*-
'''
Creates the database used by the app.
Edit the path in feeder/database.py to change
where the database is created.

By default it is:
    ./feeder.db.

where ./ refers to the location of the feeder module
'''
from feeder import db

db.create_all()

exit("Database created successfully")
