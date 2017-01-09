FROM python:3.6
MAINTAINER Jonas Kalderstam, jonas@kalderstam.se

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# This order speeds up development
COPY requirements.txt /usr/src/app/
RUN pip install --no-cache-dir -r requirements.txt
COPY . /usr/src/app

# Make sure data directory exists, though it should be mounted as a volume
RUN mkdir -p /data

ENTRYPOINT ["/usr/src/app/run.sh"]

# for sync: docker run ..... feeder sync
# for server: docker run ... feeder

# Run server
#CMD [ "python", "./runserver.py", "-c", "/data/config.yaml" ]
# Run sync
#CMD [ "python", "./runsync.py", "-c", "/data/config.yaml" ]
