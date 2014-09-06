GAE=/home/jonas/Downloads/google_appengine
DEVSERVER=$(GAE)/dev_appserver.py
APPCFG=$(GAE)/appcfg.py

# http://localhost:8080/_ah/api/explorer
local:
	$(DEVSERVER) --host=0.0.0.0 --port=9988 ./

clear:
	$(DEVSERVER) --clear_datastore=yes --host=0.0.0.0 --port=9988 ./

deploy:
	$(APPCFG) update --oauth2  ./
