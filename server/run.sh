#!/bin/bash

if [[ "$1" == "sync" ]]; then
  echo "syncing"
  python ./runsync.py -c /data/config.yaml
else
  python ./runserver.py -c /data/config.yaml
fi
