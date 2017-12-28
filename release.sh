#!/bin/bash

set -u

TARGET="${1:-HEAD}"


current_default="$(git describe --tags --abbrev=0 "${TARGET}")"


echo >&2 -n "Current version [${current_default}]: "
read -r current_in

if [ -z "${current_in}" ]; then
  CURRENT_VERSION="${current_default}"
else
  CURRENT_VERSION="${current_in}"
fi

next_default="$(cat app/build.gradle | grep "versionName" | sed "s|\s*versionName \"\(.*\)\"|\\1|")"
echo >&2 -n "Next version [${next_default}]: "
read -r next_in

if [ -z "${next_in}" ]; then
  NEXT_VERSION="${next_default}"
else
  NEXT_VERSION="${next_in}"
fi

CURRENT_CODE="$(cat app/build.gradle | grep "versionCode" | sed "s|\s*versionCode\s*\([0-9]\+\)|\\1|")"
echo >&2 "Current code ${CURRENT_CODE}"

let next_code_default=CURRENT_CODE+1

echo >&2 -n "Next code [${next_code_default}]: "
read -r next_code_in

if [ -z "${next_code_in}" ]; then
  NEXT_CODE="${next_code_default}"
else
  NEXT_CODE="${next_code_in}"
fi


CL="# ${NEXT_VERSION}
"

# Merge commits since last version
for mc in $(git rev-list --min-parents=2 "^${CURRENT_VERSION}" "${TARGET}"); do
  # grep --only-matching "\![[:digit:]]\+"
  # grep --only-matching "#[[:digit:]]\+"
  mr="$(git show --no-patch --format=%b "${mc}" | grep --only-matching "![[:digit:]]\+")"

  # All commits contained in merge
  mbase="$(git merge-base "${mc}~" "${mc}")"
  for cc in $(git rev-list "^${mbase}" "${mc}"); do
    if [[ "${cc}" == "${mc}" ]]; then
      continue
    fi
    issues="$(git show --no-patch --format=%B "${cc}" | grep --only-matching "#[[:digit:]]\+")"
    author="$(git show --no-patch --format=%an "${cc}")"

    # Transform newlines to spaces by echoing with no quotes
    issues="$(echo ${issues})"

    CL="${CL}*   $(git show --no-patch --format=%s "${cc}")"

    # Trailing whitespace to force linebreak
    CL="${CL}  
    Thanks to ${author}"

    if ! [[ -z "${issues// }" ]] || ! [[ -z "${mr// }" ]]; then
      # Two trailing whitespace to force linebreak
      CL="${CL}  
    See"
    fi

    if ! [[ -z "${mr// }" ]]; then
      CL="${CL} ${mr}"
    fi

    if ! [[ -z "${issues// }" ]]; then
      CL="${CL} ${issues}"
    fi

    CL="${CL}
"
  done
done

tmpfile="$(mktemp)"

echo "${CL}" > "${tmpfile}"

sensible-editor "${tmpfile}"

echo >&2 "Changelog for [${NEXT_VERSION}]:"
cat >&2 "${tmpfile}"

read -r -p "Write changelog? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  cat "${tmpfile}" >"fastlane/metadata/android/en-US/changelogs/${NEXT_CODE}.txt"

  PREV=""
  if [ -f CHANGELOG.md ]; then
    read -r -d '' PREV <CHANGELOG.md
  fi

  cat >CHANGELOG.md <<EOF
$(cat "${tmpfile}")

${PREV}
EOF
fi

read -r -p "Update gradle versions? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  sed -i "s|\(\s*versionCode\s*\)[0-9]\+|\\1${NEXT_CODE}|" app/build.gradle
  sed -i "s|\(\s*versionName\s*\).*|\\1\"${NEXT_VERSION}\"|" app/build.gradle
fi

read -r -p "Commit changes? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  git add "fastlane/metadata/android/en-US/changelogs/${NEXT_CODE}.txt"
  git add app/build.gradle
  git add CHANGELOG.md
  git commit -m "Releasing ${NEXT_VERSION}"
fi


read -r -p "Make tag? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  git tag -asm "$(cat "${tmpfile}")" "${NEXT_VERSION}"
fi
