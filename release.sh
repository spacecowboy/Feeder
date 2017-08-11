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

    # Transform newlines to spaces by echoing with no quotes
    issues="$(echo ${issues})"

    CL="${CL}
*   $(git show --no-patch --format=%s "${cc}")"

    if ! [[ -z "${issues// }" ]] || ! [[ -z "${mr// }" ]]; then
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

  PREV=""
  if [ -f CHANGELOG.md ]; then
    read -r -d '' PREV <CHANGELOG.md
  fi

  cat >CHANGELOG.md <<EOF
$(cat "${tmpfile}")

${PREV}
EOF
fi

read -r -p "Make tag? [y/N] " response
if [[ "$response" =~ ^[yY]$ ]]
then
  git tag -asm "$(cat "${tmpfile}")" "${NEXT_VERSION}"
fi
