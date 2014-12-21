# Starting with Debian
FROM debian:jessie
MAINTAINER Jonas Kalderstam <jonas@kalderstam.se>
RUN apt-get update
# Install requirements
RUN apt-get install -y python3 python3-pip git sqlite
RUN pip3 install uwsgi
# Clone and setup the server
RUN cd && git clone https://github.com/spacecowboy/Feeder.git && cd Feeder/server/flaskapp/ && pip3 install -r requirements.txt
