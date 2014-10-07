# -*- coding: utf-8 -*-
'''
Synchronizes all known RSS feeds. This should probably be
run as a cronscript:

Run:
    crontab -e

Then enter a line like this (syncs every 10 minutes):

    */10 * * * * /path/to/python /path/to/feeder/flaskapp/runsync.py
'''
from feeder import sync


print("Caching feeds...")
sync.cache_all_feeds()
print("Caching complete")
