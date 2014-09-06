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


"""Interface to the BackendService that serves API configurations."""


import logging

from protorpc import message_types
from protorpc import messages
from protorpc import remote

package = 'google.appengine.endpoints'


__all__ = [
    'GetApiConfigsRequest',
    'LogMessagesRequest',
    'ApiConfigList',
    'BackendService',
    'package',
]


class GetApiConfigsRequest(messages.Message):
  """Request body for fetching API configs."""
  appRevision = messages.StringField(1)


class ApiConfigList(messages.Message):
  """List of API configuration file contents."""
  items = messages.StringField(1, repeated=True)


class LogMessagesRequest(messages.Message):
  """Request body for log messages sent by Swarm FE."""

  class LogMessage(messages.Message):
    """A single log message within a LogMessagesRequest."""

    class Level(messages.Enum):
      """Levels that can be specified for a log message."""
      debug = logging.DEBUG
      info = logging.INFO
      warning = logging.WARNING
      error = logging.ERROR
      critical = logging.CRITICAL

    level = messages.EnumField(Level, 1)
    message = messages.StringField(2, required=True)

  messages = messages.MessageField(LogMessage, 1, repeated=True)


class BackendService(remote.Service):
  """API config enumeration service used by Google API Server.

  This is a simple API providing a list of APIs served by this App Engine
  instance.  It is called by the Google API Server during app deployment
  to get an updated interface for each of the supported APIs.
  """



  @remote.method(GetApiConfigsRequest, ApiConfigList)
  def getApiConfigs(self, request):
    """Return a list of active APIs and their configuration files.

    Args:
      request: A request which may contain an app revision

    Returns:
      List of ApiConfigMessages
    """
    raise NotImplementedError()

  @remote.method(LogMessagesRequest, message_types.VoidMessage)
  def logMessages(self, request):
    """Write a log message from the Swarm FE to the log.

    Args:
      request: A log message request.

    Returns:
      Void message.
    """
    raise NotImplementedError()
