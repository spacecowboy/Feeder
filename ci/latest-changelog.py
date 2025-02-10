#!/usr/bin/env python3

import sys


def main(args):
    buffer = []
    should_append = False

    with open(args[0], "r") as f:
        for line in f:
            if line.startswith("## "):
                if buffer:
                    break
                should_append = True

            if should_append:
                buffer.append(line)

    for line in buffer:
        print(line, end="")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: latest-changelog.py <changelog>")
        sys.exit(1)

    main(sys.argv[1:])
