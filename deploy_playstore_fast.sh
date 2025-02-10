#!/bin/bash -eu

LATEST_TAG="$(git describe --tags "$(git rev-list --tags --max-count=1)")"
CURRENT_VERSION="$(git describe --tags)"

if [[ "${1:-}" == "--dry-run" ]]; then
  echo "Dry run was specified. Validating deployment"
  fastlane validate_deploy track:internal
elif [[ "${LATEST_TAG}" == "${CURRENT_VERSION}" ]] && [[ "${CURRENT_VERSION}" =~ ^[0-9.]*$ ]]; then
  echo "${CURRENT_VERSION} is a tag - deploying to production!"
  fastlane deploy track:internal
  fastlane promote track:internal track_promote_to:alpha
  fastlane promote track:alpha track_promote_to:beta
  fastlane promote track:beta track_promote_to:production
else
  echo "${CURRENT_VERSION} is a pre release - deploying to beta!"
  fastlane deploy track:internal
  fastlane promote track:internal track_promote_to:alpha
  fastlane promote track:alpha track_promote_to:beta
fi
