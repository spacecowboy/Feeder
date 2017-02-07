# -*- coding: utf-8 -*-
'''
Synchronizes all known RSS feeds. This should probably be
run as a cronscript:

Run:
    crontab -e

Then enter a line like this (syncs every 10 minutes):

    */10 * * * * /path/to/python /path/to/feeder/flaskapp/runsync.py [-c configfile]
'''
from feeder import read_config
import sys
import os


if __name__ == '__main__':
    args = sys.argv[1:]
    if '-c' in args:
        i = args.index('-c')
        configfile = args[i + 1]
    else:
        configfile = 'config.yaml'

    if not os.path.isfile(configfile):
        exit("Config file '{}' does not exist. A config file is required.".format(os.path.abspath(configfile)))

    # Read config and configure database path
    read_config(configfile)

    # Only import after config reading
    from feeder import sync
    print("Caching feeds...")
    sync.cache_all_feeds()
    print("Caching complete")
