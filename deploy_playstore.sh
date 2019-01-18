#!/bin/bash -eu

cat > app/creds.b64 <<EOF
${SERVICEACCOUNTJSON}
EOF

base64 --ignore-garbage --decode app/creds.b64 > app/creds.json

cat > keystore.b64 <<EOF
${KEYSTORE}
EOF

base64 --ignore-garbage --decode keystore.b64 > keystore

cat >> gradle.properties <<EOF
STORE_FILE=$(pwd)/keystore
STORE_PASSWORD=${KEYSTOREPASSWORD}
KEY_ALIAS=${KEYALIAS}
KEY_PASSWORD=${KEYPASSWORD}
EOF

VERSION_CODE="$(cat app/build.gradle | grep "versionCode" | sed "s|\s*versionCode\s*\([0-9]\+\)|\\1|")"
readonly VERSION_CODE

FASTLANE_CL="fastlane/metadata/android/en-US/changelogs/${VERSION_CODE}.txt"
readonly FASTLANE_CL

mkdir -p app/src/main/play/release-notes/en-GB/

# Play store (and thus plugin) has a limit of 500 characters
head --bytes=500 "${FASTLANE_CL}" > app/src/main/play/release-notes/en-GB/default.txt

./gradlew publishPlayReleaseBundle
