Also add a crontab entry. If you named the container "feeder":

# Twice an hour
5,35 * * * *   /usr/bin/docker exec feeder /sync.sh
