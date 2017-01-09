# -*- coding: utf-8 -*-
'''
Usage:
    python runserver.py [-d][-h 127.0.0.1][-p 5000]

This small script runs the flask REST app.

It might be sensible to run it with the debug switch: -d.
This reloads the server when it detects a change in the code.
Never use the debug switch in production!

The arguments are as follows:

  -d - enable debug mode. Default is False.
  -h - specify the listening address. Use 0.0.0.0 to listen on all.
  -p - specify the listening port. Defaults to 5000.
  -c - specify the config file. Defaults to config.yaml but is mandatory.

Note that any arguments specified will override
duplicates in the configfile.
'''
import os
import sys
from feeder import app, read_config


def ensure_db_exists():
    '''
    Tell Flask to create the database which is configured if it doesn't exist
    '''
    filepath = app.config.get('FEEDER_DATABASE', './feeder.db')
    filepath = os.path.abspath(filepath)

    if not os.path.isfile(filepath):
        from feeder.database import db
        db.create_all()
        print("Database was created successfully")


def run_flask_app(kw):
    app.run(**kw)


def run_sync_loop(delay=600):
    """
    - delay : time between syncs in seconds.
    """


if __name__ == '__main__':
    args = sys.argv[1:]

    # Put config/arguments here
    kw = {}

    if '-c' in args:
        i = args.index('-c')
        configfile = args[i + 1]
    else:
        configfile = 'config.yaml'

    if not os.path.isfile(configfile):
        exit("Config file '{}' does not exist. A config file is required.".format(os.path.abspath(configfile)))

    for k, v in read_config(configfile).items():
        if k in 'debug,host,port'.split(','):
            # Lowercase as kwargs in run
            kw[k] = v

    # If arguments are specified, they override config values
    if '--help' in args:
        exit(__doc__)
    if '-d' in args:
        kw['debug'] = True
    if '-h' in args:
        i = args.index('-h')
        kw['host'] = args[i + 1]
    if '-p' in args:
        i = args.index('-p')
        kw['port'] = int(args[i + 1])

    # This configures rest api to run. Do it AFTER config loading
    import feeder.rest
    # After rest import
    ensure_db_exists()

    run_flask_app(kw)
    #app.run(**kw)
