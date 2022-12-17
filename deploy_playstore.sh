#!/bin/bash -eu

LATEST_TAG="$(git describe --tags "$(git rev-list --tags --max-count=1)")"
CURRENT_VERSION="$(git describe --tags)"

if [ -n "${SERVICEACCOUNTJSON:-}" ]; then
  cat > app/creds.b64 <<EOF
${SERVICEACCOUNTJSON}
EOF
fi

base64 --ignore-garbage --decode app/creds.b64 > app/creds.json

sed -i "s|/home/jonas/.ssh/service.json|$(pwd)/app/creds.json|" fastlane/Appfile

if [ -n "${KEYSTORE:-}" ]; then
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
ci/delete-unwanted-langs

if [[ "${1:-}" == "--dry-run" ]] && [[ "${LATEST_TAG}" == "${CURRENT_VERSION}" ]]; then
  # Gitlab runs master and tag pipelines concurrently and fastlane will conflict if run concurrently
  echo "${CURRENT_VERSION} is a tag but --dry-run was specified - not doing anything"
elif [[ "${1:-}" == "--dry-run" ]] || [[ "${LATEST_TAG}" != "${CURRENT_VERSION}" ]]; then
  echo "${CURRENT_VERSION} is not tag - validating deployment"
  if [[ "${CURRENT_VERSION}" =~ ^[0-9.]*$ ]]; then
    echo "${CURRENT_VERSION} is a production release"
    fastlane build_and_validate track:production
  else
    echo "${CURRENT_VERSION} is a beta release"
    fastlane build_and_validate track:beta
  fi
else
  echo "${CURRENT_VERSION} is a tag - deploying to store!"
  if [[ "${CURRENT_VERSION}" =~ ^[0-9.]*$ ]]; then
    echo "${CURRENT_VERSION} is a production release"
    fastlane build_and_deploy track:production
  else
    echo "${CURRENT_VERSION} is a beta release"
    fastlane build_and_deploy track:beta
  fi
fi

git checkout app fastlane gradle.properties
