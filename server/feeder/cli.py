# -*- coding: utf-8 -*-
'''
Cache RSS feeds and serve them with a REST API.
'''

_syncdoc='''
Synchronizes all known RSS feeds. This should probably be
run as a cronjob. See the bundled feeder-sync.timer.
'''

_serverdoc='''
Run a webserver which serves cached RSS feeds over a REST
API. Command line options override defined values in config file, if
present.
'''

_pwdoc='''
Prints the hashed version of the passwords given, one per line.
Note that whitespace is stripped.
'''

_userdoc='''
Add, modify, or remove a user from the database.
Passwords are stored hashed and salted.
'''


import os
import argparse
from feeder.version import __version__


def ensure_config(path):
    if not os.path.isfile(path):
        exit("Config file '{}' does not exist. A config file is required.".format(os.path.abspath(path)))


def ensure_db_exists():
    '''
    Tell Flask to create the database which is configured if it doesn't exist
    '''
    import feeder.rest
    from feeder.database import app, db

    filepath = app.config.get('FEEDER_DATABASE', '/var/lib/feeder/feeder.db')
    filepath = os.path.abspath(filepath)

    if not os.path.isfile(filepath):
        print("Ensuring database")
        db.create_all()
        print("Database was created successfully")


def sync(args):
    ensure_config(args.config)

    from feeder import read_config

    # Read config and configure database path
    read_config(args.config)

    # Only import after config reading
    ensure_db_exists()
    from feeder import sync
    print("Caching feeds...")
    sync.cache_all_feeds()
    print("Caching complete")


def server(args):
    ensure_config(args.config)
    from feeder import read_config

    # Put config/arguments here
    kw = {}

    for k, v in read_config(args.config).items():
        if k in 'debug,host,port'.split(','):
            # Lowercase as kwargs in run
            kw[k] = v

    if args.debug:
        kw['debug'] = True

    if args.host:
        kw['host'] = args.host

    if args.port:
        kw['port'] = args.port

    ensure_db_exists()
    # This configures rest api to run. Do it AFTER config loading
    #import feeder.rest
    from feeder.rest import app
    app.run(**kw)


def hash_password(args):
    from hashlib import sha1
    from werkzeug.security import generate_password_hash
    from feeder.constants import __ANDROID_SALT__

    for pw in args.password:
        bpassword = pw.strip().encode('utf-8')
        # Then add the salt used by the android client
        androidpassword = sha1(__ANDROID_SALT__ + bpassword)\
            .hexdigest().lower()

        # And finally salt it for real
        print(generate_password_hash(androidpassword))


def add_user(args):
    config = args.config
    username = args.username
    password = args.password
    remove = args.remove

    ensure_config(config)
    from feeder import read_config

    read_config(config)
    ensure_db_exists()

    from hashlib import sha1
    from werkzeug.security import generate_password_hash
    from feeder.database import db
    from feeder.models import get_user
    from feeder.constants import __ANDROID_SALT__

    if remove:
        user = get_user(username, allow_creation=False)
        if user:
            db.session.delete(user)
            db.session.commit()
            print("Removed user", username)
        else:
            print("No such user. Nothing to do.")
    else:
        if not password:
            from getpass import getpass
            password = getpass()

        # Generate a password hash
        # Make sure to use a byte string
        try:
            bpassword = password.encode('utf-8')
        except AttributeError:
            # Already bytestring
            bpassword = password

        # Then add the salt used by the android client
        androidpassword = sha1(__ANDROID_SALT__ + bpassword)\
            .hexdigest().lower()

        # And finally salt it for real
        user = get_user(username, allow_creation=True)
        user.passwordhash = generate_password_hash(androidpassword)

        db.session.add(user)
        db.session.commit()
        print("Updated user", username)


def get_parser(formatter_class=None):
    '''
    Returns the primary arguments parser
    '''
    return get_parsers(formatter_class)[0]


def get_parsers(formatter_class=None):
    '''
    Returns a list of the primary arguments parser and subcommand parsers.
    '''
    result = []
    if formatter_class:
        parser = argparse.ArgumentParser(prog='feeder', description=__doc__,
                                         formatter_class=formatter_class)
    else:
        parser = argparse.ArgumentParser(prog='feeder', description=__doc__)


    parser.add_argument('--version', action='version', version='%(prog)s ' + __version__)

    result.append(parser)

    subparsers = parser.add_subparsers(help='',
                                       title='commands')

    parser_sync = subparsers.add_parser('sync',
                                        help='synchronize stored RSS feeds',
                                        description=_syncdoc)
    parser_sync.add_argument('-c', '--config',
                             help='path to config file',
                             default='/etc/feeder/config.yaml')
    parser_sync.set_defaults(func=sync)
    result.append(parser_sync)

    parser_server = subparsers.add_parser('server',
                                          help='serve the REST API',
                                          description=_serverdoc)
    parser_server.add_argument('--debug', help='run in debug mode',
                               action='store_true')
    parser_server.add_argument('--host', help='host address to bind to')
    parser_server.add_argument('--port', help='port to bind to')
    parser_server.add_argument('-c', '--config',
                               help='path to config file',
                               default='/etc/feeder/config.yaml')
    parser_server.set_defaults(func=server)
    result.append(parser_server)

    parser_pass = subparsers.add_parser('pwhash',
                                        help='hash a password',
                                        description=_pwdoc)
    parser_pass.add_argument('password', nargs='+',
                             help='passwords to hash')
    parser_pass.set_defaults(func=hash_password)
    result.append(parser_pass)

    parser_adduser = subparsers.add_parser('user',
                                           help='add/set/remove users to the database',
                                           description=_userdoc)

    parser_adduser.add_argument('username',
                                help='username to modify')
    parser_adduser.add_argument('password', nargs='?',
                                help='desired password, read from stdin if missing')
    parser_adduser.add_argument('--remove', help='remove a user from the database',
                                action='store_true')
    parser_adduser.add_argument('-c', '--config',
                                help='path to config file',
                                default='/etc/feeder/config.yaml')
    parser_adduser.set_defaults(func=add_user)
    result.append(parser_adduser)

    return result
