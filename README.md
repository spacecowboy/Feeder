<a href="https://flattr.com/submit/auto?user_id=spacecowboy&url=https%3A%2F%2Fgithub.com%2Fspacecowboy%2FFeeder" target="_blank"><img src="http://api.flattr.com/button/flattr-badge-large.png" alt="Flattr this" title="Flattr this" border="0"></a>

### License

**GPLv2**, for more info see *LICENSE*.

### Quick install

Clone the project:

    git clone --recursive https://github.com/spacecowboy/Feeder.git

Then build and install the app to your phone which is connected via USB:

    ./gradlew installProdDebug

My server is configured by default and you can use your Google account (no password required) to login.

### More details

This is an RSS app for Android.
Unlike some simple feed readers out there, this app interfaces with a server 
which is included in the `server/flaskapp` directory. The server allows easy
configuration with a yaml-file and you can setup the server to only allow
certain usernames/password. You can also configure it to accept a valid Google token
so people can login via their Google accounts, without giving up their passwords
(they only have to reveal their email addresses).
Furthermore, you can also allow
anyone with a Google account to login if you so desire.

The app is currently useless without a server to talk to as all the feed processing
and parsing happens serverside. This is done to increase the speed (substantially)
and allows synchronizations to be made extremely fast. Parsing the feeds on device 
can be quite slow. Future versions of the server might allow anonymous usage, or
the app might allow device local parsing. I care about speed and that just isn't there
at the moment.

### Features

* Offline reading
* Lightning fast synchronization
* Notification support
* OPML Import/Export
* Material desing

### How to run your own server?

Either directly on your machine if you have Python3 installed, or
using Docker, which is the most fool-proof way.

See [server/flaskapp](server/flaskapp).

### Screenshots

<img src="graphics/Screenshot_2014-12-28-00-43-24.png" width=50%/>
<img src="graphics/Screenshot_2014-12-28-00-43-37.png" width=50%/>
<img src="graphics/Screenshot_2014-12-28-00-43-46.png" width=50%/>
<img src="graphics/Screenshot_2014-12-28-00-44-02.png" width=50%/>
<img src="graphics/Screenshot_2014-12-28-00-44-18.png" width=50%/>
