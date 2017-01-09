## Feeder REST API

This server app caches RSS-feeds so the client app doesn't miss out on
anything. Since RSS-feeds typically only hold 10 items or so, old
items also go away quickly if there are alot of updates. Since the
items are cached in the database here, more items will be available
than in the feeds themselves.

The app is written in [Python3](http://www.python.org) using the
[Flask](http://flask.pocoo.org/) framework with database details
handled by [SQLAlchemy](http://www.sqlalchemy.org/). And no, it is not
compatible with Python2.  The reason is timezone implementations for
[datetimes](https://docs.python.org/3.4/library/datetime.html), which
was added in Python3.  If you want Python2 compatibility, the datetime
functions will need a bit of work.

Before you can run the server you need to install the requirements

    pip install -r requirements.txt

and create the database:

    python createdb.py -c config.yaml

Now, you can run the server:

    python runserver.py -c config.yaml

You might want to give some options to it. For developing, it's nice
to run it in debug mode. You can also specify the listening address
and port number. An example with all options specified (host and port
are default values):

    python runserver.py -d -h 127.0.0.1 -p 5000

For use with Docker do:

```
docker run --rm --name=feeder -v /path/to/data:/data spacecowboy/feeder
```

To synchronize feeds, you can do (in a crontab or something)

```
docker run --rm --name=feeder -v /path/to/data:/data spacecowboy/feeder python runsync.py -c /data/config.yaml
```
