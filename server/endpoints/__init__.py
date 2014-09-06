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




"""Apiserving Module."""






from api_config import api
from api_config import API_EXPLORER_CLIENT_ID
from api_config import AUTH_LEVEL
from api_config import EMAIL_SCOPE
from api_config import method
from api_config import ResourceContainer
from api_exceptions import *
from apiserving import *
import message_parser
from users_id_token import get_current_user
from users_id_token import InvalidGetUserCall
from users_id_token import SKIP_CLIENT_ID_CHECK

__version__ = '1.0'
