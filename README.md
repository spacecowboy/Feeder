### License

**GPLv2**, for more info see *LICENSE*.

### How to clone

Since I have a submodule in this repo, remember to clone recursively:

    git clone --recursive https://github.com/spacecowboy/Feeder.git

This is equivalent to doing it in two steps:

    git clone https://github.com/spacecowboy/Feeder.git
    git submodule update --init --recursive

### How to build Android client?

Quick install:

    ./gradlew installPlayDebug

For more possible options, see:

    ./gradlew tasks

The client is currently hardcoded to sync with the server I'm running.
It requires you to authenticate with your Google account so you don't
mess with other people's feeds. Note that no passwords are transmitted
or stored. Only an auth-token (time limited access code) is requested
on device which only authorizes me to check what e-mail address requested
the token.

The app is useless without the server. Future versions of the server
will support simple username/password to be able to build the app without
play services available.

### How to run the server yourself?

See [server/flaskapp](server/flaskapp).
