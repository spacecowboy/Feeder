# -*- coding: utf-8 -*-
from time import time


def timestamp():
    '''
    timestamp() -> integer number

    Returns current time in milliseconds since the Epoch.
    '''
    return int(1000 * time())
