#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""Utility library for reading user information from an id_token.

This is an experimental library that can temporarily be used to extract
a user from an id_token.  The functionality provided by this library
will be provided elsewhere in the future.
"""


import base64

try:
  import json
except ImportError:
  import simplejson as json
import logging
import os
import re
import time
import urllib

try:

  from google.appengine.api import memcache
  from google.appengine.api import oauth
  from google.appengine.api import urlfetch
  from google.appengine.api import users
except ImportError:

  from google.appengine.api import memcache
  from google.appengine.api import oauth
  from google.appengine.api import urlfetch
  from google.appengine.api import users

try:





  from Crypto.Hash import SHA256
  from Crypto.PublicKey import RSA

  _CRYPTO_LOADED = True
except ImportError:
  _CRYPTO_LOADED = False


__all__ = ['get_current_user',
           'InvalidGetUserCall',
           'SKIP_CLIENT_ID_CHECK']

SKIP_CLIENT_ID_CHECK = ['*']
_CLOCK_SKEW_SECS = 300
_MAX_TOKEN_LIFETIME_SECS = 86400
_DEFAULT_CERT_URI = ('https://www.googleapis.com/service_accounts/v1/metadata/'
                     'raw/federated-signon@system.gserviceaccount.com')
_ENV_USE_OAUTH_SCOPE = 'ENDPOINTS_USE_OAUTH_SCOPE'
_ENV_AUTH_EMAIL = 'ENDPOINTS_AUTH_EMAIL'
_ENV_AUTH_DOMAIN = 'ENDPOINTS_AUTH_DOMAIN'
_EMAIL_SCOPE = 'https://www.googleapis.com/auth/userinfo.email'
_TOKENINFO_URL = 'https://www.googleapis.com/oauth2/v1/tokeninfo'
_MAX_AGE_REGEX = re.compile(r'\s*max-age\s*=\s*(\d+)\s*')
_CERT_NAMESPACE = '__verify_jwt'


class _AppIdentityError(Exception):
  pass


class InvalidGetUserCall(Exception):
  """Called get_current_user when the environment was not set up for it."""



def get_current_user():
  """Get user information from the id_token or oauth token in the request.

  This should only be called from within an Endpoints request handler,
  decorated with an @endpoints.method decorator.  The decorator should include
  the https://www.googleapis.com/auth/userinfo.email scope.

  If the current request uses an id_token, this validates and parses the token
  against the info in the current request handler and returns the user.
  Or, for an Oauth token, this call validates the token against the tokeninfo
  endpoint and oauth.get_current_user with the scopes provided in the method's
  decorator.

  Returns:
    None if there is no token or it's invalid.  If the token was valid, this
      returns a User.  Only the user's email field is guaranteed to be set.
      Other fields may be empty.

  Raises:
    InvalidGetUserCall: if the environment variables necessary to determine the
      endpoints user are not set. These are typically set when processing a
      request using an Endpoints handler. If they are not set, it likely
      indicates that this function was called from outside an Endpoints request
      handler.
  """
  if not _is_auth_info_available():

    raise InvalidGetUserCall('No valid endpoints user in environment.')

  if _ENV_USE_OAUTH_SCOPE in os.environ:




    return oauth.get_current_user(os.environ[_ENV_USE_OAUTH_SCOPE])

  if (_ENV_AUTH_EMAIL in os.environ and
      _ENV_AUTH_DOMAIN in os.environ):
    if not os.environ[_ENV_AUTH_EMAIL]:


      return None


    return users.User(os.environ[_ENV_AUTH_EMAIL],
                      os.environ[_ENV_AUTH_DOMAIN] or None)



  return None



def _is_auth_info_available():
  """Check if user auth info has been set in environment variables."""
  return ((_ENV_AUTH_EMAIL in os.environ and
           _ENV_AUTH_DOMAIN in os.environ) or
          _ENV_USE_OAUTH_SCOPE in os.environ)


def _maybe_set_current_user_vars(method, api_info=None, request=None):
  """Get user information from the id_token or oauth token in the request.

  Used internally by Endpoints to set up environment variables for user
  authentication.

  Args:
    method: The class method that's handling this request.  This method
      should be annotated with @endpoints.method.
    api_info: An api_config._ApiInfo instance. Optional. If None, will attempt
      to parse api_info from the implicit instance of the method.
    request: The current request, or None.
  """
  if _is_auth_info_available():
    return


  os.environ[_ENV_AUTH_EMAIL] = ''
  os.environ[_ENV_AUTH_DOMAIN] = ''




  try:
    api_info = api_info or method.im_self.api_info
  except AttributeError:





    logging.warning('AttributeError when accessing %s.im_self.  An unbound '
                    'method was probably passed as an endpoints handler.',
                    method.__name__)
    scopes = method.method_info.scopes
    audiences = method.method_info.audiences
    allowed_client_ids = method.method_info.allowed_client_ids
  else:
    scopes = (method.method_info.scopes
              if method.method_info.scopes is not None
              else api_info.scopes)
    audiences = (method.method_info.audiences
                 if method.method_info.audiences is not None
                 else api_info.audiences)
    allowed_client_ids = (method.method_info.allowed_client_ids
                          if method.method_info.allowed_client_ids is not None
                          else api_info.allowed_client_ids)

  if not scopes and not audiences and not allowed_client_ids:



    return

  token = _get_token(request)
  if not token:

    return None





  if ((scopes == [_EMAIL_SCOPE] or scopes == (_EMAIL_SCOPE,)) and
      allowed_client_ids):

    logging.debug('Checking for id_token.')
    time_now = long(time.time())
    user = _get_id_token_user(token, audiences, allowed_client_ids, time_now,
                              memcache)

    if user:
      os.environ[_ENV_AUTH_EMAIL] = user.email()
      os.environ[_ENV_AUTH_DOMAIN] = user.auth_domain()

      return


  if scopes:
    logging.debug('Checking for oauth token.')
    if _is_local_dev():
      _set_bearer_user_vars_local(token, allowed_client_ids, scopes)
    else:
      _set_bearer_user_vars(allowed_client_ids, scopes)



def _get_token(request):
  """Get the auth token for this request.

  Auth token may be specified in either the Authorization header or
  as a query param (either access_token or bearer_token).  We'll check in
  this order:
    1. Authorization header.
    2. bearer_token query param.
    3. access_token query param.

  Args:
    request: The current request, or None.

  Returns:
    The token in the request or None.
  """

  auth_header = os.environ.get('HTTP_AUTHORIZATION')

  if auth_header:
    allowed_auth_schemes = ('OAuth', 'Bearer')
    for auth_scheme in allowed_auth_schemes:
      if auth_header.startswith(auth_scheme):

        return auth_header[len(auth_scheme) + 1:]


    return None


  if request:
    for key in ('bearer_token', 'access_token'):
      token, _ = request.get_unrecognized_field_info(key)
      if token:

        return token



def _get_id_token_user(token, audiences, allowed_client_ids, time_now, cache):
  """Get a User for the given id token, if the token is valid.

  Args:
    token: The id_token to check.
    audiences: List of audiences that are acceptable.
    allowed_client_ids: List of client IDs that are acceptable.
    time_now: The current time as a long (eg. long(time.time())).
    cache: Cache to use (eg. the memcache module).

  Returns:
    A User if the token is valid, None otherwise.
  """


  try:
    parsed_token = _verify_signed_jwt_with_certs(token, time_now, cache)
  except _AppIdentityError, e:
    logging.debug('id_token verification failed: %s', e)
    return None
  except:
    logging.debug('id_token verification failed.')
    return None

  if _verify_parsed_token(parsed_token, audiences, allowed_client_ids):
    email = parsed_token['email']






    return users.User(email)



def _set_oauth_user_vars(token_info, audiences, allowed_client_ids, scopes,
                         local_dev):
  logging.warning('_set_oauth_user_vars is deprecated and will be removed '
                  'soon.')
  return _set_bearer_user_vars(allowed_client_ids, scopes)



def _set_bearer_user_vars(allowed_client_ids, scopes):
  """Validate the oauth bearer token and set endpoints auth user variables.

  If the bearer token is valid, this sets ENDPOINTS_USE_OAUTH_SCOPE.  This
  provides enough information that our endpoints.get_current_user() function
  can get the user.

  Args:
    allowed_client_ids: List of client IDs that are acceptable.
    scopes: List of acceptable scopes.
  """
  for scope in scopes:
    try:
      client_id = oauth.get_client_id(scope)
    except oauth.Error:

      continue




    if (list(allowed_client_ids) != SKIP_CLIENT_ID_CHECK and
        client_id not in allowed_client_ids):
      logging.warning('Client ID is not allowed: %s', client_id)
      return

    os.environ[_ENV_USE_OAUTH_SCOPE] = scope
    logging.debug('Returning user from matched oauth_user.')
    return

  logging.debug('Oauth framework user didn\'t match oauth token user.')
  return None


def _set_bearer_user_vars_local(token, allowed_client_ids, scopes):
  """Validate the oauth bearer token on the dev server.

  Since the functions in the oauth module return only example results in local
  development, this hits the tokeninfo endpoint and attempts to validate the
  token.  If it's valid, we'll set _ENV_AUTH_EMAIL and _ENV_AUTH_DOMAIN so we
  can get the user from the token.

  Args:
    token: String with the oauth token to validate.
    allowed_client_ids: List of client IDs that are acceptable.
    scopes: List of acceptable scopes.
  """

  result = urlfetch.fetch(
      '%s?%s' % (_TOKENINFO_URL, urllib.urlencode({'access_token': token})))
  if result.status_code != 200:
    try:
      error_description = json.loads(result.content)['error_description']
    except (ValueError, KeyError):
      error_description = ''
    logging.error('Token info endpoint returned status %s: %s',
                  result.status_code, error_description)
    return
  token_info = json.loads(result.content)


  if 'email' not in token_info:
    logging.warning('Oauth token doesn\'t include an email address.')
    return
  if not token_info.get('verified_email'):
    logging.warning('Oauth token email isn\'t verified.')
    return


  client_id = token_info.get('issued_to')
  if (list(allowed_client_ids) != SKIP_CLIENT_ID_CHECK and
      client_id not in allowed_client_ids):
    logging.warning('Client ID is not allowed: %s', client_id)
    return


  token_scopes = token_info.get('scope', '').split(' ')
  if not any(scope in scopes for scope in token_scopes):
    logging.warning('Oauth token scopes don\'t match any acceptable scopes.')
    return

  os.environ[_ENV_AUTH_EMAIL] = token_info['email']
  os.environ[_ENV_AUTH_DOMAIN] = ''
  logging.debug('Local dev returning user from token.')
  return


def _is_local_dev():
  return os.environ.get('SERVER_SOFTWARE', '').startswith('Development')


def _verify_parsed_token(parsed_token, audiences, allowed_client_ids):

  if parsed_token.get('iss') != 'accounts.google.com':
    logging.warning('Issuer was not valid: %s', parsed_token.get('iss'))
    return False


  aud = parsed_token.get('aud')
  if not aud:
    logging.warning('No aud field in token')
    return False



  cid = parsed_token.get('azp')
  if aud != cid and aud not in audiences:
    logging.warning('Audience not allowed: %s', aud)
    return False


  if list(allowed_client_ids) == SKIP_CLIENT_ID_CHECK:
    logging.warning('Client ID check can\'t be skipped for ID tokens.  '
                    'Id_token cannot be verified.')
    return False
  elif not cid or cid not in allowed_client_ids:
    logging.warning('Client ID is not allowed: %s', cid)
    return False

  if 'email' not in parsed_token:
    return False

  return True


def _urlsafe_b64decode(b64string):

  b64string = b64string.encode('ascii')
  padded = b64string + '=' * ((4 - len(b64string)) % 4)
  return base64.urlsafe_b64decode(padded)


def _get_cert_expiration_time(headers):
  """Get the expiration time for a cert, given the response headers.

  Get expiration time from the headers in the result.  If we can't get
  a time from the headers, this returns 0, indicating that the cert
  shouldn't be cached.

  Args:
    headers: A dict containing the response headers from the request to get
      certs.

  Returns:
    An integer with the number of seconds the cert should be cached.  This
    value is guaranteed to be >= 0.
  """

  cache_control = headers.get('Cache-Control', '')



  for entry in cache_control.split(','):
    match = _MAX_AGE_REGEX.match(entry)
    if match:
      cache_time_seconds = int(match.group(1))
      break
  else:
    return 0


  age = headers.get('Age')
  if age is not None:
    try:
      age = int(age)
    except ValueError:
      age = 0
    cache_time_seconds -= age

  return max(0, cache_time_seconds)


def _get_cached_certs(cert_uri, cache):
  certs = cache.get(cert_uri, namespace=_CERT_NAMESPACE)
  if certs is None:
    logging.debug('Cert cache miss')
    try:
      result = urlfetch.fetch(cert_uri)
    except AssertionError:

      return None

    if result.status_code == 200:
      certs = json.loads(result.content)
      expiration_time_seconds = _get_cert_expiration_time(result.headers)
      if expiration_time_seconds:
        cache.set(cert_uri, certs, time=expiration_time_seconds,
                  namespace=_CERT_NAMESPACE)
    else:
      logging.error(
          'Certs not available, HTTP request returned %d', result.status_code)

  return certs


def _b64_to_long(b):
  b = b.encode('ascii')
  b += '=' * ((4 - len(b)) % 4)
  b = base64.b64decode(b)
  return long(b.encode('hex'), 16)


def _verify_signed_jwt_with_certs(
    jwt, time_now, cache,
    cert_uri=_DEFAULT_CERT_URI):
  """Verify a JWT against public certs.

  See http://self-issued.info/docs/draft-jones-json-web-token.html.

  The PyCrypto library included with Google App Engine is severely limited and
  so you have to use it very carefully to verify JWT signatures. The first
  issue is that the library can't read X.509 files, so we make a call to a
  special URI that has the public cert in modulus/exponent form in JSON.

  The second issue is that the RSA.verify method doesn't work, at least for
  how the JWT tokens are signed, so we have to manually verify the signature
  of the JWT, which means hashing the signed part of the JWT and comparing
  that to the signature that's been encrypted with the public key.

  Args:
    jwt: string, A JWT.
    time_now: The current time, as a long (eg. long(time.time())).
    cache: Cache to use (eg. the memcache module).
    cert_uri: string, URI to get cert modulus and exponent in JSON format.

  Returns:
    dict, The deserialized JSON payload in the JWT.

  Raises:
    _AppIdentityError: if any checks are failed.
  """

  segments = jwt.split('.')

  if len(segments) != 3:
    raise _AppIdentityError('Wrong number of segments in token: %s' % jwt)
  signed = '%s.%s' % (segments[0], segments[1])

  signature = _urlsafe_b64decode(segments[2])



  lsignature = long(signature.encode('hex'), 16)


  header_body = _urlsafe_b64decode(segments[0])
  try:
    header = json.loads(header_body)
  except:
    raise _AppIdentityError('Can\'t parse header: %s' % header_body)
  if header.get('alg') != 'RS256':
    raise _AppIdentityError('Unexpected encryption algorithm: %s' %
                            header.get('alg'))


  json_body = _urlsafe_b64decode(segments[1])
  try:
    parsed = json.loads(json_body)
  except:
    raise _AppIdentityError('Can\'t parse token: %s' % json_body)

  certs = _get_cached_certs(cert_uri, cache)
  if certs is None:
    raise _AppIdentityError(
        'Unable to retrieve certs needed to verify the signed JWT: %s' % jwt)



  if not _CRYPTO_LOADED:
    raise _AppIdentityError('Unable to load pycrypto library.  Can\'t verify '
                            'id_token signature.  See http://www.pycrypto.org '
                            'for more information on pycrypto.')



  local_hash = SHA256.new(signed).hexdigest()


  verified = False
  for keyvalue in certs['keyvalues']:
    modulus = _b64_to_long(keyvalue['modulus'])
    exponent = _b64_to_long(keyvalue['exponent'])
    key = RSA.construct((modulus, exponent))


    hexsig = '%064x' % key.encrypt(lsignature, '')[0]

    hexsig = hexsig[-64:]



    verified = (hexsig == local_hash)
    if verified:
      break
  if not verified:
    raise _AppIdentityError('Invalid token signature: %s' % jwt)


  iat = parsed.get('iat')
  if iat is None:
    raise _AppIdentityError('No iat field in token: %s' % json_body)
  earliest = iat - _CLOCK_SKEW_SECS


  exp = parsed.get('exp')
  if exp is None:
    raise _AppIdentityError('No exp field in token: %s' % json_body)
  if exp >= time_now + _MAX_TOKEN_LIFETIME_SECS:
    raise _AppIdentityError('exp field too far in future: %s' % json_body)
  latest = exp + _CLOCK_SKEW_SECS

  if time_now < earliest:
    raise _AppIdentityError('Token used too early, %d < %d: %s' %
                            (time_now, earliest, json_body))
  if time_now > latest:
    raise _AppIdentityError('Token used too late, %d > %d: %s' %
                            (time_now, latest, json_body))

  return parsed
