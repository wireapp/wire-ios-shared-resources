#! /bin/bash

set -euf -o pipefail

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
  xcodebuild -workspace "Wire-iOS.xcworkspace" -scheme "${SCHEME}" -destination "${DESTINATION}" -derivedDataPath DerivedData build-for-testing | XCPRETTY_JSON_FILE_OUTPUT=xcodebuild.json bundle exec xcpretty -f `bundle exec xcpretty-json-formatter`
else
  xcodebuild -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" -derivedDataPath DerivedData analyze build-for-testing | XCPRETTY_JSON_FILE_OUTPUT=xcodebuild.json bundle exec xcpretty -f `bundle exec xcpretty-json-formatter`
fi
