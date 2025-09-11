#!/bin/bash

set -e
# Copy current snapshots to a temp folder (ignore if there is no existing folder)
cp -R app-screenshot-tests/src/test/snapshots/ tmpSnapshots || mkdir tmpSnapshots

# Regenerate snapshots
./gradlew recordPaparazziDebug

# Copy previous snapshots back to ensure we only create new snapshot, not delete old ones
cp -R tmpSnapshots/. app-screenshot-tests/src/test/snapshots/
rm -rf ./tmpSnapshots

git add app-screenshot-tests/src/test/snapshots

if [[ -n $(git status --porcelain=v1 | grep "^[A|M|D|R]") ]]
then
  # Use Github Actions bot address https://github.com/orgs/community/discussions/26560
  git config user.name github-actions[bot]
  git config user.email 41898282+github-actions[bot]@users.noreply.github.com
  git commit -m "chore: add new screenshot tests"
  # Pushing large amounts of screenshot something fails. Push LFS screenshots first.
  git lfs push --all
  git push origin HEAD:$1
fi
