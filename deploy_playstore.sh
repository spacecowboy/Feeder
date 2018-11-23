#!/bin/bash -eu

cat > app/creds.b64 <<EOF
${SERVICEACCOUNTJSON}
EOF

base64 --ignore-garbage --decode app/creds.b64 > app/creds.json

VERSION_CODE="$(cat app/build.gradle | grep "versionCode" | sed "s|\s*versionCode\s*\([0-9]\+\)|\\1|")"
readonly VERSION_CODE

FASTLANE_CL="fastlane/metadata/android/en-US/changelogs/${VERSION_CODE}.txt"
readonly FASTLANE_CL

mkdir -p app/src/main/play/release-notes/en-US/

cat "${FASTLANE_CL}" > app/src/main/play/release-notes/en-US/default.txt

./gradlew publishPlayReleaseBundle
