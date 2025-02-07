#!/bin/bash -eu

TARGET="${1:-HEAD}"

current_default="$(git describe --tags --abbrev=0 "${TARGET}")"

echo >&2 -n "Current version [${current_default}]"

next_default="$(git cliff --bumped-version)"
echo >&2 -n "Next version [${next_default}]: "
read -r next_in

if [ -z "${next_in}" ]; then
  NEXT_VERSION="${next_default}"
else
  NEXT_VERSION="${next_in}"
fi

CURRENT_CODE="$(grep "versionCode" app/build.gradle.kts | sed "s|\s*versionCode = \([0-9]\+\)|\\1|")"
echo >&2 "Current code ${CURRENT_CODE}"

next_code_default=$(( CURRENT_CODE+1 ))

echo >&2 -n "Next code [${next_code_default}]: "
read -r next_code_in

if [ -z "${next_code_in}" ]; then
  NEXT_CODE="${next_code_default}"
else
  NEXT_CODE="${next_code_in}"
fi

# Get rid of these to make the build reproducible
ci/delete-unwanted-langs

read -r -p "Update locales_config.xml? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  ./gradlew --no-configuration-cache :app:generateLocalesConfig
  git add app/src/main/res/xml/locales_config.xml
fi

CL="$(git cliff --tag "${NEXT_VERSION}" --unreleased --strip header --strip footer)"
echo >&2 "${CL}"

read -r -p "Write changelog? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  git cliff --tag "${NEXT_VERSION}" -o CHANGELOG.md
  git add CHANGELOG.md
fi

echo "Verifying build"
./gradlew check pixel2api30DebugAndroidTest || echo >&2 "Build failed"

read -r -p "Update gradle versions? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  sed -i "s|\(\s*versionCode = \)[0-9]\+|\\1${NEXT_CODE}|" app/build.gradle.kts
  sed -i "s|\(\s*versionName = \).*|\\1\"${NEXT_VERSION}\"|" app/build.gradle.kts
fi

read -r -p "Commit changes? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  git add app/build.gradle.kts
  git diff --staged
  git commit -m "chore: releasing ${NEXT_VERSION}"
fi

read -r -p "Make tag? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  git tag -asm "$CL" "${NEXT_VERSION}"
fi

# Undo the changes to locales_config.xml
git checkout app/src/main/res fastlane/metadata/android

read -r -p "Post to feed? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  scripts/changelog-to-hugo.main.kts  ../feeder-news/content/posts/ "${NEXT_VERSION}"
  pushd ../feeder-news
  git add content/posts/
  git diff --staged
  git commit -m "Released ${NEXT_VERSION}"
  popd
fi

read -r -p "Push the lot? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  git push --follow-tags
  pushd ../feeder-news
  git push
  popd
fi
