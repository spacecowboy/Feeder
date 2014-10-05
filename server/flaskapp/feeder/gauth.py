"""
Handles validating client bearer tokens.
"""
from flask import request, abort
from httplib2 import Http
import json


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

    return data['email']


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
            print("No token in header")
            abort(401)
            return None

        print("Checking token...")
        userid = validate_token(request.headers['Authorization'])
        if userid is None:
            print("Check returned FAIL!")
            # Unauthorized
            abort(401)
            return None

        return fn(userid=userid, *args, **kwargs)
    return _wrap
