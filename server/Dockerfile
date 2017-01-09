## Feeder server
## To run this correctly, use commands such as this:
## docker build -t feeder .
## Followed by:
## docker run --name feeder_rss -p 5000:5000 -v /path/to/dirwith/configfile:/feederserver -i -t feeder
## It will mount the directory specifed inside the container. Make sure to define
## the database directory as "./" in the config file (or not at all). That way, the
## database is created in that same directory.

FROM python:3.6
MAINTAINER Jonas Kalderstam, jonas@kalderstam.se

# First install the basic requirements, like Python3
#RUN apt-get update && apt-get install -y \
#    build-essential \
#    gcc \
#    git \
#    python3.4 \
#    python3.4-dev \
#    libyaml-dev \
#    && apt-get autoremove \
#    && apt-get clean

# Distro politics prevents pip from being installed correctly
#ADD https://github.com/pypa/pip/raw/645180e2714b4ffcf40363a608239e089c9dafab/contrib/get-pip.py /root/get-pip.py

#RUN python3.4 /root/get-pip.py

# Updating pip is always a good idea
#RUN pip3.4 install --upgrade "pip"

# Clone the repo
#RUN git clone https://github.com/spacecowboy/Feeder.git /feeder

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# This order speeds up development
COPY requirements.txt /usr/src/app/
RUN pip install --no-cache-dir -r requirements.txt
COPY . /usr/src/app
#ONBUILD COPY . /usr/src/app

# Install requirements
#RUN pip3.4 install -r /feeder/server/flaskapp/requirements.txt

#COPY run.sh /run.sh
#COPY sync.sh /sync.sh

# Make sure data directory exists, though it should be mounted as a volume
RUN mkdir -p /data

# Run server
CMD [ "python", "./runserver.py -c /data/config.yaml" ]
#CMD /run.sh

# To sync, do this command when the server is running:
# docker exec [-it] feeder /sync.sh
