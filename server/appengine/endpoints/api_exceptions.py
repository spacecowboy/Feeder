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
"""A library containing exception types used by Endpoints ProtoRPC services."""



import httplib

from protorpc import remote


class ServiceException(remote.ApplicationError):
  """Base class for request/service exceptions in Endpoints."""

  def __init__(self, message=None):
    super(ServiceException, self).__init__(message,
                                           httplib.responses[self.http_status])


class BadRequestException(ServiceException):
  """Bad request exception that is mapped to a 400 response."""
  http_status = httplib.BAD_REQUEST


class ForbiddenException(ServiceException):
  """Forbidden exception that is mapped to a 403 response."""
  http_status = httplib.FORBIDDEN


class InternalServerErrorException(ServiceException):
  """Internal server exception that is mapped to a 500 response."""
  http_status = httplib.INTERNAL_SERVER_ERROR


class NotFoundException(ServiceException):
  """Not found exception that is mapped to a 404 response."""
  http_status = httplib.NOT_FOUND


class UnauthorizedException(ServiceException):
  """Unauthorized exception that is mapped to a 401 response."""
  http_status = httplib.UNAUTHORIZED
