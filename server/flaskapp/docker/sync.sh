#!/bin/bash -eu

# Config and database is stored in /data, mount that as a volume

# If no config file exists, exit with message
if [ ! -f "/data/config.yaml" ]; then
  echo "A configuration file (config.yaml) must be present in"
  echo "the /data directory. A sample file is as follows:"
  echo ""
  cat /feeder/server/flaskapp/config-sample.yaml
  echo ""
  echo "Will now exit. Goodbye!"
  exit 1
fi

python3.4 /feeder/server/flaskapp/runsync.py -c /data/config.yaml
