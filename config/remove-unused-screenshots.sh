#!/bin/bash

set -e

./gradlew cleanRecordPaparazziDebug

git add -A app-screenshot-tests/src/test/snapshots

if [[ -n $(git status --porcelain=v1 | grep "^[A|M|D|R]") ]]
then
  # Use Github Actions bot address https://github.com/orgs/community/discussions/26560
  git config user.name github-actions[bot]
  git config user.email 41898282+github-actions[bot]@users.noreply.github.com
  git commit -m "chore: remove unused screenshot tests"
  git push origin HEAD:$1
fi
