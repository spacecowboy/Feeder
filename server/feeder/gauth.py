# -*- coding: utf-8 -*-
"""
Handles validating client bearer tokens, or
that the user/password is correct.
"""
from flask import request, abort, current_app
from httplib2 import Http
import json
import base64
from .models import get_user
from hashlib import sha1
from werkzeug.security import check_password_hash, generate_password_hash
from .constants import __ANDROID_SALT__


def validate_token(access_token):
    '''Verifies that an access-token is valid and
    meant for this app.

    Returns None on fail, and an e-mail on success'''
    h = Http()
    resp, cont = h.request("https://www.googleapis.com/oauth2/v2/userinfo",
                           headers={'Host': 'www.googleapis.com',
                                    'Authorization': access_token})

    if not resp['status'] == '200':
        return None

    try:
        data = json.loads(cont)
    except TypeError:
        # httplib2 returns byte objects
        data = json.loads(cont.decode())

    email = data['email']
    users = current_app.config.get('FEEDER_USERS', {})
    if current_app.config.get('FEEDER_ALLOW_ANY_GOOGLE', False):
        return email
    elif email in users:
        return email
    else:
        return None


def validate_userpass(credentials):
    '''
    Validate the username/password provided.

    Returns None failure, and a username on success
    '''
    # Get the BASE64 encoded username:password
    enc_userpass = credentials.replace('Basic ', '')
    # Decode BASE64 from bytes string
    userpass = base64.b64decode(enc_userpass.encode('UTF-8'))
    # Need to change back to String
    userpass = userpass.decode('UTF-8')

    try:
        username, password = userpass.split(':')
        # Enforce lowercase on password hash
        password = password.lower()
        # Check validity of username password
        users = current_app.config.get('FEEDER_USERS', {})

        #user = get_user(username, allow_creation=False)
        #if user is None or user.passwordhash is None:
        if username not in users:
            # User does not exist
            return None

        valid = False
        # Get password
        userpass = users[username]
        # Stored hashed
        if userpass.startswith('pbkdf2:sha1'):
            valid = check_password_hash(userpass, password)
        else:
            # Generate android hash if stored in plaintext
            userpasshash = sha1(__ANDROID_SALT__ + userpass.encode('utf-8')).hexdigest().lower()
            valid = (userpasshash == password)

        if not valid:
            # Try really plain text
            valid = (userpass == password)

        if valid:
            return username
        else:
            None
    except:
        # invalid user/pass
        return None

    # Must be invalid...
    return None


def is_basic(credentials):
    '''Returns True if credentials are of username:password type'''
    return credentials.startswith('Basic ')


def authorized(fn):
    """Decorator that checks that requests
    contain an id-token in the request header.
    userid will be None if the
    authentication failed, and have an id otherwise.

    Usage:
    @app.route("/")
    @authorized
    def secured_root(userid=None):
        pass
    """

    def _wrap(*args, **kwargs):
        if 'Authorization' not in request.headers:
            # Unauthorized
            abort(401)
            return None

        userid = None
        credentials = request.headers['Authorization']
        if (is_basic(credentials) and
                current_app.config['FEEDER_ALLOW_USERPASS']):
            userid = validate_userpass(credentials)
        elif (not is_basic(credentials) and
                current_app.config['FEEDER_ALLOW_GOOGLE']):
            userid = validate_token(credentials)

        if userid is None:
            # Unauthorized
            abort(401)
            return None

        return fn(userid=userid, *args, **kwargs)
    return _wrap
