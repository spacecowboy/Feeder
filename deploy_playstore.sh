#!/bin/bash -eu

LATEST_TAG="$(git describe --tags "$(git rev-list --tags --max-count=1)")"
CURRENT_VERSION="$(git describe --tags)"

if [[ "${LATEST_TAG}" != "${CURRENT_VERSION}" ]]; then
  echo "${CURRENT_VERSION} is not tag - not deploying release"
  exit 0
else
  echo "${CURRENT_VERSION} is a tag - deploying to store!"
fi

cat > app/creds.b64 <<EOF
${SERVICEACCOUNTJSON}
EOF

base64 --ignore-garbage --decode app/creds.b64 > app/creds.json

sed -i "s|/home/jonas/.ssh/service.json|$(pwd)/app/creds.json|" fastlane/Appfile

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

fastlane deploy
