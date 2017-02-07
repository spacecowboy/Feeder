#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import sys
from .cli import *


if __name__ == "__main__":
    args = sys.argv[1:]
    parser = get_parser()

    if len(args) == 0:
        parser.print_help()
        exit(1)

    args = parser.parse_args(args)
    args.func(args)
