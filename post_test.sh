#! /bin/bash

EXIT=0

echo "Downloading ${DEPENDENCIES_BASE_URL}/Dangerfile"
curl -O "${DEPENDENCIES_BASE_URL}/Dangerfile" || EXIT=$?
bundle exec danger || EXIT=$?

if [ "$IS_UI_PROJECT" -eq "1" ]; 
then
  echo "Skipping code coverage for UI project"
else
  echo "Uploading coverage to codecov.io"
  bash <(curl -s https://codecov.io/bash) -J "^${SCHEME}$" -D DerivedData -X xcodellvm || EXIT=$?
fi

# Fail if any of the commands had exit status != 0
exit $EXIT
