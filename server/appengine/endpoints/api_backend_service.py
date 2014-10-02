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


"""Api serving config collection service implementation.

Contains the implementation for BackendService as defined in api_backend.py.
"""



try:
  import json
except ImportError:
  import simplejson as json
import logging

from endpoints import api_backend
from endpoints import api_config
from endpoints import api_exceptions
from protorpc import message_types


__all__ = [
    'ApiConfigRegistry',
    'BackendServiceImpl',
]


class ApiConfigRegistry(object):
  """Registry of active APIs to be registered with Google API Server."""

  def __init__(self):

    self.__registered_classes = set()

    self.__api_configs = set()

    self.__api_methods = {}


  def register_spi(self, config_contents):
    """Register a single SPI and its config contents.

    Args:
      config_contents: String containing API configuration.
    """
    if config_contents is None:
      return
    parsed_config = json.loads(config_contents)
    self.__register_class(parsed_config)
    self.__api_configs.add(config_contents)
    self.__register_methods(parsed_config)

  def __register_class(self, parsed_config):
    """Register the class implementing this config, so we only add it once.

    Args:
      parsed_config: The JSON object with the API configuration being added.

    Raises:
      ApiConfigurationError: If the class has already been registered.
    """
    methods = parsed_config.get('methods')
    if not methods:
      return


    service_classes = set()
    for method in methods.itervalues():
      rosy_method = method.get('rosyMethod')
      if rosy_method and '.' in rosy_method:
        method_class = rosy_method.split('.', 1)[0]
        service_classes.add(method_class)

    for service_class in service_classes:
      if service_class in self.__registered_classes:
        raise api_config.ApiConfigurationError(
            'SPI class %s has already been registered.' % service_class)
      self.__registered_classes.add(service_class)

  def __register_methods(self, parsed_config):
    """Register all methods from the given api config file.

    Methods are stored in a map from method_name to rosyMethod,
    the name of the ProtoRPC method to be called on the backend.
    If no rosyMethod was specified the value will be None.

    Args:
      parsed_config: The JSON object with the API configuration being added.
    """
    methods = parsed_config.get('methods')
    if not methods:
      return

    for method_name, method in methods.iteritems():
      self.__api_methods[method_name] = method.get('rosyMethod')

  def lookup_api_method(self, api_method_name):
    """Looks an API method up by name to find the backend method to call.

    Args:
      api_method_name: Name of the method in the API that was called.

    Returns:
      Name of the ProtoRPC method called on the backend, or None if not found.
    """
    return self.__api_methods.get(api_method_name)

  def all_api_configs(self):
    """Return a list of all API configration specs as registered above."""
    return list(self.__api_configs)


class BackendServiceImpl(api_backend.BackendService):
  """Implementation of BackendService."""

  def __init__(self, api_config_registry, app_revision):
    """Create a new BackendService implementation.

    Args:
      api_config_registry: ApiConfigRegistry to register and look up configs.
      app_revision: string containing the current app revision.
    """
    self.__api_config_registry = api_config_registry
    self.__app_revision = app_revision




  @staticmethod
  def definition_name():
    """Override definition_name so that it is not BackendServiceImpl."""
    return api_backend.BackendService.definition_name()

  def getApiConfigs(self, request):
    """Return a list of active APIs and their configuration files.

    Args:
      request: A request which may contain an app revision

    Returns:
      ApiConfigList: A list of API config strings
    """
    if request.appRevision and request.appRevision != self.__app_revision:
      raise api_exceptions.BadRequestException(
          message='API backend app revision %s not the same as expected %s' % (
              self.__app_revision, request.appRevision))

    configs = self.__api_config_registry.all_api_configs()
    return api_backend.ApiConfigList(items=configs)

  def logMessages(self, request):
    """Write a log message from the Swarm FE to the log.

    Args:
      request: A log message request.

    Returns:
      Void message.
    """
    Level = api_backend.LogMessagesRequest.LogMessage.Level
    log = logging.getLogger(__name__)
    for message in request.messages:
      level = message.level if message.level is not None else Level.info



      record = logging.LogRecord(name=__name__, level=level.number, pathname='',
                                 lineno='', msg=message.message, args=None,
                                 exc_info=None)
      log.handle(record)

    return message_types.VoidMessage()
