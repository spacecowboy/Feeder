#!/bin/bash

# Use current directory as the configdir
DIR=$(pwd)

# Naming it feeder_rss. Running on port 5000.
# Last argument is the name specified when building the image (feeder)
docker start feeder_rss || docker run --name feeder_rss -p 5000:5000 -v $DIR:/feederserver -i -t feeder
