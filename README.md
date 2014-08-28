### License

**GPLv2**, for more info see *LICENSE*.

### How to clone

Since I have a submodule in this repo, remember to clone recursively:

    git clone --recursive https://github.com/spacecowboy/Feeder.git

This is equivalent to doing it in two steps:

    git clone https://github.com/spacecowboy/Feeder.git
    git submodule update --init --recursive

### How to build

Quick install:

    ./gradlew installKitkatDebug

For more possible options, see:

    ./gradlew tasks
