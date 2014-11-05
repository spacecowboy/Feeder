# -*- coding: utf-8 -*-
'''
Usage:
    python runserver.py [-d][-h 127.0.0.1][-p 5000]

Example usage with uWSGI:
    uwsgi --socket 127.0.0.1:8080 -w runserver:app

This small script runs the flask REST app. For development
it is possible to run the app directly:

    python runserver.py

It might be sensible to run it with the debug switch: -d.
This reloads the server when it detects a change in the code.
Never use the debug switch in production!

The arguments are as follows:

  -d - enable debug mode. Default is False.
  -h - specify the listening address. Use 0.0.0.0 to listen on all.
  -p - specify the listening port. Defaults to 5000.

In production it might be sensible to run this with something like
uwsgi behind nginx. Note that any arguments specified will override
duplicates in the configfile.

You specify a configfile by setting the environment variable:

    export FEEDER_CONFIG=/path/to/settings.py

You can call the file whatever you like but it will be read like a
python file. Example configuration would be:

    # Example config
    DEBUG = False
    ## Use /// for relative paths and //// for absolute paths
    SQLALCHEMY_DATABASE_URI = 'sqlite:////path/to/test.db'
    FEEDER_ALLOW_GOOGLE = True
    FEEDER_ALLOW_USERPASS = True

'''
from feeder import app


if __name__ == '__main__':
    import sys

    # Put config/arguments here
    kw = {}

    # If arguments are specified, they override config values
    args = sys.argv[1:]
    if '--help' in args:
        exit(__doc__)
    if '-d' in args:
        kw['debug'] = True
    if '-h' in args:
        i = args.index('-h')
        kw['host'] = args[i + 1]
    if '-p' in args:
        i = args.index('-p')
        kw['port'] = args[i + 1]

    app.run(**kw)
