fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android build_play_bundle

```sh
[bundle exec] fastlane android build_play_bundle
```

Build play bundle

### android deploy

```sh
[bundle exec] fastlane android deploy
```

Deploy a new version to the Google Play

### android validate_deploy

```sh
[bundle exec] fastlane android validate_deploy
```

Validate deployment of a new version to the Google Play

### android promote

```sh
[bundle exec] fastlane android promote
```

Promotes between tracks

### android build_and_deploy

```sh
[bundle exec] fastlane android build_and_deploy
```

Build and deploy a new version to the Google Play

### android build_and_validate

```sh
[bundle exec] fastlane android build_and_validate
```

Build and validate deployment of a new version to the Google Play

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
