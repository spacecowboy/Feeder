#!/bin/bash -eu

LATEST_TAG="$(git describe --tags "$(git rev-list --tags --max-count=1)")"
CURRENT_VERSION="$(git describe --tags)"

echo "Current version: ${CURRENT_VERSION}"
echo "Latest tag: ${LATEST_TAG}"

if [[ "${1:-}" == "--dry-run" ]]; then
  echo "Dry run was specified. Validating deployment"
  fastlane validate_deploy track:internal|| { echo "Validation failed"; exit 1; }
else
  echo "Deploying to beta!"
  fastlane deploy track:internal || { echo "Deployment failed"; exit 1; }
  fastlane promote track:internal track_promote_to:alpha|| { echo "Promotion to alpha failed"; exit 1; }
  fastlane promote track:alpha track_promote_to:beta || { echo "Promotion to beta failed"; exit 1; }

  if [[ "${LATEST_TAG}" == "${CURRENT_VERSION}" ]] && [[ "${CURRENT_VERSION}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "${CURRENT_VERSION} is a release - deploying to production!"
    fastlane promote track:beta track_promote_to:production|| { echo "Promotion to production failed"; exit 1; }
  fi
fi
