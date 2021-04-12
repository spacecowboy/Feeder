#!/bin/bash -eu

LATEST_TAG="$(git describe --tags "$(git rev-list --tags --max-count=1)")"
CURRENT_VERSION="$(git describe --tags)"

if [ ! -z "${SERVICEACCOUNTJSON:-}" ]; then
  cat > app/creds.b64 <<EOF
${SERVICEACCOUNTJSON}
EOF
fi

base64 --ignore-garbage --decode app/creds.b64 > app/creds.json

sed -i "s|/home/jonas/.ssh/service.json|$(pwd)/app/creds.json|" fastlane/Appfile

if [ ! -z "${KEYSTORE:-}" ]; then
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

fi

# Delete unsupported google play store languages
rm -rf fastlane/metadata/android/bs-BA \
   fastlane/metadata/android/el \
   fastlane/metadata/android/ru \
   fastlane/metadata/android/eo

if [[ "${1:-}" == "--dry-run" ]] && [[ "${LATEST_TAG}" == "${CURRENT_VERSION}" ]]; then
  # Gitlab runs master and tag pipelines concurrently and fastlane will conflict if run concurrently
  echo "${CURRENT_VERSION} is a tag but --dry-run was specified - not doing anything"
elif [[ "${1:-}" == "--dry-run" ]] || [[ "${LATEST_TAG}" != "${CURRENT_VERSION}" ]]; then
  echo "${CURRENT_VERSION} is not tag - validating deployment"
  fastlane build_and_validate
else
  echo "${CURRENT_VERSION} is a tag - deploying to store!"
  fastlane build_and_deploy
fi

git checkout app fastlane gradle.properties
