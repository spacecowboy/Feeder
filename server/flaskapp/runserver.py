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
uwsgi behind nginx.
'''
from feeder import app


if __name__ == '__main__':
    import sys

    kw = {}
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
