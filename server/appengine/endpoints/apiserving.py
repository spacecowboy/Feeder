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


"""A library supporting use of the Google API Server.

This library helps you configure a set of ProtoRPC services to act as
Endpoints backends.  In addition to translating ProtoRPC to Endpoints
compatible errors, it exposes a helper service that describes your services.

  Usage:
  1) Create an endpoints.api_server instead of a webapp.WSGIApplication.
  2) Annotate your ProtoRPC Service class with @endpoints.api to give your
     API a name, version, and short description
  3) To return an error from Google API Server raise an endpoints.*Exception
     The ServiceException classes specify the http status code returned.

     For example:
     raise endpoints.UnauthorizedException("Please log in as an admin user")


  Sample usage:
  - - - - app.yaml - - - -

  handlers:
  # Path to your API backend.
  - url: /_ah/spi/.*
    # For the legacy python runtime this would be "script: services.py"
    script: services.app

  - - - - services.py - - - -

  import endpoints
  import postservice

  app = endpoints.api_server([postservice.PostService], debug=True)

  - - - - postservice.py - - - -

  @endpoints.api(name='guestbook', version='v0.2', description='Guestbook API')
  class PostService(remote.Service):
    ...
    @endpoints.method(GetNotesRequest, Notes, name='notes.list', path='notes',
                       http_method='GET')
    def list(self, request):
      raise endpoints.UnauthorizedException("Please log in as an admin user")
"""


import cgi
import cStringIO
import httplib
import os

from endpoints import api_backend_service
from endpoints import api_config
from endpoints import api_exceptions
from endpoints import protojson
from protorpc import messages
from protorpc import remote
from protorpc.wsgi import service as wsgi_service

package = 'google.appengine.endpoints'


__all__ = [
    'api_server',
    'EndpointsErrorMessage',
    'package',
]


_ERROR_NAME_MAP = dict((httplib.responses[c.http_status], c) for c in [
    api_exceptions.BadRequestException,
    api_exceptions.ForbiddenException,
    api_exceptions.InternalServerErrorException,
    api_exceptions.NotFoundException,
    api_exceptions.UnauthorizedException,
    ])

_ALL_JSON_CONTENT_TYPES = frozenset(
    [protojson.EndpointsProtoJson.CONTENT_TYPE] +
    protojson.EndpointsProtoJson.ALTERNATIVE_CONTENT_TYPES)





class EndpointsErrorMessage(messages.Message):
  """Message for returning error back to Google Endpoints frontend.

  Fields:
    state: State of RPC, should be 'APPLICATION_ERROR'.
    error_message: Error message associated with status.
  """

  class State(messages.Enum):
    """Enumeration of possible RPC states.

    Values:
      OK: Completed successfully.
      RUNNING: Still running, not complete.
      REQUEST_ERROR: Request was malformed or incomplete.
      SERVER_ERROR: Server experienced an unexpected error.
      NETWORK_ERROR: An error occured on the network.
      APPLICATION_ERROR: The application is indicating an error.
        When in this state, RPC should also set application_error.
    """
    OK = 0
    RUNNING = 1

    REQUEST_ERROR = 2
    SERVER_ERROR = 3
    NETWORK_ERROR = 4
    APPLICATION_ERROR = 5
    METHOD_NOT_FOUND_ERROR = 6

  state = messages.EnumField(State, 1, required=True)
  error_message = messages.StringField(2)



def _get_app_revision(environ=None):
  """Gets the app revision (minor app version) of the current app.

  Args:
    environ: A dictionary with a key CURRENT_VERSION_ID that maps to a version
      string of the format <major>.<minor>.

  Returns:
    The app revision (minor version) of the current app, or None if one couldn't
    be found.
  """
  if environ is None:
    environ = os.environ
  if 'CURRENT_VERSION_ID' in environ:
    return environ['CURRENT_VERSION_ID'].split('.')[1]


class _ApiServer(object):
  """ProtoRPC wrapper, registers APIs and formats errors for Google API Server.

  - - - - ProtoRPC error format - - - -
  HTTP/1.0 400 Please log in as an admin user.
  content-type: application/json

  {
    "state": "APPLICATION_ERROR",
    "error_message": "Please log in as an admin user",
    "error_name": "unauthorized",
  }

  - - - - Reformatted error format - - - -
  HTTP/1.0 401 UNAUTHORIZED
  content-type: application/json

  {
    "state": "APPLICATION_ERROR",
    "error_message": "Please log in as an admin user"
  }
  """


  __SPI_PREFIX = '/_ah/spi/'
  __BACKEND_SERVICE_ROOT = '%sBackendService' % __SPI_PREFIX
  __SERVER_SOFTWARE = 'SERVER_SOFTWARE'




  __IGNORE_RESTRICTION_PREFIXES = ('Development/', 'WSGIServer/', 'testutil/')
  __HEADER_NAME_PEER = 'HTTP_X_APPENGINE_PEER'
  __GOOGLE_PEER = 'apiserving'


  __PROTOJSON = protojson.EndpointsProtoJson()

  def __init__(self, api_services, **kwargs):
    """Initialize an _ApiServer instance.

    The primary function of this method is to set up the WSGIApplication
    instance for the service handlers described by the services passed in.
    Additionally, it registers each API in ApiConfigRegistry for later use
    in the BackendService.getApiConfigs() (API config enumeration service).

    Args:
      api_services: List of protorpc.remote.Service classes implementing the API
        or a list of _ApiDecorator instances that decorate the service classes
        for an API.
      **kwargs: Passed through to protorpc.wsgi.service.service_handlers except:
        protocols - ProtoRPC protocols are not supported, and are disallowed.
        restricted - If True or unset, the API will only be allowed to serve to
          Google's API serving infrastructure once deployed.  Set to False to
          allow other clients.  Under dev_appserver, all clients are accepted.
          NOTE! Under experimental launch, this is not a secure restriction and
          other authentication mechanisms *must* be used to control access to
          the API.  The restriction is only intended to notify developers of
          a possible upcoming feature to securely restrict access to the API.

    Raises:
      TypeError: if protocols are configured (this feature is not supported).
      ApiConfigurationError: if there's a problem with the API config.
    """
    for entry in api_services[:]:

      if isinstance(entry, api_config._ApiDecorator):
        api_services.remove(entry)
        api_services.extend(entry.get_api_classes())

    self.api_config_registry = api_backend_service.ApiConfigRegistry()
    api_name_version_map = self.__create_name_version_map(api_services)
    protorpc_services = self.__register_services(api_name_version_map,
                                                 self.api_config_registry)


    backend_service = api_backend_service.BackendServiceImpl.new_factory(
        self.api_config_registry, _get_app_revision())
    protorpc_services.insert(0, (self.__BACKEND_SERVICE_ROOT, backend_service))


    if 'protocols' in kwargs:
      raise TypeError('__init__() got an unexpected keyword argument '
                      "'protocols'")
    protocols = remote.Protocols()
    protocols.add_protocol(self.__PROTOJSON, 'protojson')
    remote.Protocols.set_default(protocols)

    self.restricted = kwargs.pop('restricted', True)
    self.service_app = wsgi_service.service_mappings(protorpc_services,
                                                     **kwargs)

  @staticmethod
  def __create_name_version_map(api_services):
    """Create a map from API name/version to Service class/factory.

    This creates a map from an API name and version to a list of remote.Service
    factories that implement that API.

    Args:
      api_services: A list of remote.Service-derived classes or factories
        created with remote.Service.new_factory.

    Returns:
      A mapping from (api name, api version) to a list of service factories,
      for service classes that implement that API.

    Raises:
      ApiConfigurationError: If a Service class appears more than once
        in api_services.
    """
    api_name_version_map = {}
    for service_factory in api_services:
      try:
        service_class = service_factory.service_class
      except AttributeError:
        service_class = service_factory
        service_factory = service_class.new_factory()

      key = service_class.api_info.name, service_class.api_info.version
      service_factories = api_name_version_map.setdefault(key, [])
      if service_factory in service_factories:
        raise api_config.ApiConfigurationError(
            'Can\'t add the same class to an API twice: %s' %
            service_factory.service_class.__name__)

      service_factories.append(service_factory)
    return api_name_version_map

  @staticmethod
  def __register_services(api_name_version_map, api_config_registry):
    """Register & return a list of each SPI URL and class that handles that URL.

    This finds every service class in api_name_version_map, registers it with
    the given ApiConfigRegistry, builds the SPI url for that class, and adds
    the URL and its factory to a list that's returned.

    Args:
      api_name_version_map: A mapping from (api name, api version) to a list of
        service factories, as returned by __create_name_version_map.
      api_config_registry: The ApiConfigRegistry where service classes will
        be registered.

    Returns:
      A list of (SPI URL, service_factory) for each service class in
      api_name_version_map.

    Raises:
      ApiConfigurationError: If a Service class appears more than once
        in api_name_version_map.  This could happen if one class is used to
        implement multiple APIs.
    """
    generator = api_config.ApiConfigGenerator()
    protorpc_services = []
    for service_factories in api_name_version_map.itervalues():
      service_classes = [service_factory.service_class
                         for service_factory in service_factories]
      config_file = generator.pretty_print_config_to_json(service_classes)
      api_config_registry.register_spi(config_file)

      for service_factory in service_factories:
        protorpc_class_name = service_factory.service_class.__name__
        root = _ApiServer.__SPI_PREFIX + protorpc_class_name
        if any(service_map[0] == root or service_map[1] == service_factory
               for service_map in protorpc_services):
          raise api_config.ApiConfigurationError(
              'Can\'t reuse the same class in multiple APIs: %s' %
              protorpc_class_name)
        protorpc_services.append((root, service_factory))
    return protorpc_services

  def __is_request_restricted(self, environ):
    """Determine if access to SPI should be denied.

    Access will always be allowed in dev_appserver and under unit tests, but
    will only be allowed in production if the HTTP header HTTP_X_APPENGINE_PEER
    is set to 'apiserving'.  Google's Endpoints server sets this header by
    default and App Engine may securely prevent outside callers from setting it
    in the future to allow better protection of the API backend.

    Args:
      environ: WSGI environment dictionary.

    Returns:
      True if access should be denied, else False.
    """
    if not self.restricted:
      return False
    server = environ.get(self.__SERVER_SOFTWARE, '')
    for prefix in self.__IGNORE_RESTRICTION_PREFIXES:
      if server.startswith(prefix):
        return False
    peer_name = environ.get(self.__HEADER_NAME_PEER, '')
    return peer_name.lower() != self.__GOOGLE_PEER

  def __is_json_error(self, status, headers):
    """Determine if response is an error.

    Args:
      status: HTTP status code.
      headers: Dictionary of (lowercase) header name to value.

    Returns:
      True if the response was an error, else False.
    """
    content_header = headers.get('content-type', '')
    content_type, unused_params = cgi.parse_header(content_header)
    return (status.startswith('400') and
            content_type.lower() in _ALL_JSON_CONTENT_TYPES)

  def __write_error(self, status_code, error_message=None):
    """Return the HTTP status line and body for a given error code and message.

    Args:
      status_code: HTTP status code to be returned.
      error_message: Error message to be returned.

    Returns:
      Tuple (http_status, body):
        http_status: HTTP status line, e.g. 200 OK.
        body: Body of the HTTP request.
    """
    if error_message is None:
      error_message = httplib.responses[status_code]
    status = '%d %s' % (status_code, httplib.responses[status_code])
    message = EndpointsErrorMessage(
        state=EndpointsErrorMessage.State.APPLICATION_ERROR,
        error_message=error_message)
    return status, self.__PROTOJSON.encode_message(message)

  def protorpc_to_endpoints_error(self, status, body):
    """Convert a ProtoRPC error to the format expected by Google Endpoints.

    If the body does not contain an ProtoRPC message in state APPLICATION_ERROR
    the status and body will be returned unchanged.

    Args:
      status: HTTP status of the response from the backend
      body: JSON-encoded error in format expected by Endpoints frontend.

    Returns:
      Tuple of (http status, body)
    """
    try:
      rpc_error = self.__PROTOJSON.decode_message(remote.RpcStatus, body)
    except (ValueError, messages.ValidationError):
      rpc_error = remote.RpcStatus()

    if rpc_error.state == remote.RpcStatus.State.APPLICATION_ERROR:


      error_class = _ERROR_NAME_MAP.get(rpc_error.error_name)
      if error_class:
        status, body = self.__write_error(error_class.http_status,
                                          rpc_error.error_message)
    return status, body

  def __call__(self, environ, start_response):
    """Wrapper for Swarm server app.

    Args:
      environ: WSGI request environment.
      start_response: WSGI start response function.

    Returns:
      Response from service_app or appropriately transformed error response.
    """

    def StartResponse(status, headers, exc_info=None):
      """Save args, defer start_response until response body is parsed.

      Create output buffer for body to be written into.
      Note: this is not quite WSGI compliant: The body should come back as an
        iterator returned from calling service_app() but instead, StartResponse
        returns a writer that will be later called to output the body.
      See google/appengine/ext/webapp/__init__.py::Response.wsgi_write()
          write = start_response('%d %s' % self.__status, self.__wsgi_headers)
          write(body)

      Args:
        status: Http status to be sent with this response
        headers: Http headers to be sent with this response
        exc_info: Exception info to be displayed for this response
      Returns:
        callable that takes as an argument the body content
      """
      call_context['status'] = status
      call_context['headers'] = headers
      call_context['exc_info'] = exc_info

      return body_buffer.write

    if self.__is_request_restricted(environ):
      status, body = self.__write_error(httplib.NOT_FOUND)
      headers = [('Content-Type', 'text/plain')]
      exception = None

    else:

      call_context = {}
      body_buffer = cStringIO.StringIO()
      body_iter = self.service_app(environ, StartResponse)
      status = call_context['status']
      headers = call_context['headers']
      exception = call_context['exc_info']


      body = body_buffer.getvalue()

      if not body:
        body = ''.join(body_iter)


      headers_dict = dict([(k.lower(), v) for k, v in headers])
      if self.__is_json_error(status, headers_dict):
        status, body = self.protorpc_to_endpoints_error(status, body)

    start_response(status, headers, exception)
    return [body]




def api_server(api_services, **kwargs):
  """Create an api_server.

  The primary function of this method is to set up the WSGIApplication
  instance for the service handlers described by the services passed in.
  Additionally, it registers each API in ApiConfigRegistry for later use
  in the BackendService.getApiConfigs() (API config enumeration service).

  Args:
    api_services: List of protorpc.remote.Service classes implementing the API
      or a list of _ApiDecorator instances that decorate the service classes
      for an API.
    **kwargs: Passed through to protorpc.wsgi.service.service_handlers except:
      protocols - ProtoRPC protocols are not supported, and are disallowed.
      restricted - If True or unset, the API will only be allowed to serve to
        Google's API serving infrastructure once deployed.  Set to False to
        allow other clients.  Under dev_appserver, all clients are accepted.
        NOTE! Under experimental launch, this is not a secure restriction and
        other authentication mechanisms *must* be used to control access to
        the API.  The restriction is only intended to notify developers of
        a possible upcoming feature to securely restrict access to the API.

  Returns:
    A new WSGIApplication that serves the API backend and config registry.

  Raises:
    TypeError: if protocols are configured (this feature is not supported).
  """

  if 'protocols' in kwargs:
    raise TypeError("__init__() got an unexpected keyword argument 'protocols'")
  return _ApiServer(api_services, **kwargs)
