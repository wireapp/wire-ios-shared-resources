#! /bin/bash

set -euf -o pipefail

echo "Sourcing env-vars"
source env-vars

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
  xcodebuild -workspace "Wire-iOS.xcworkspace" -scheme "${SCHEME}" -destination "${DESTINATION}" build-for-testing | tee "xcode_build.log" | XCPRETTY_JSON_FILE_OUTPUT=xcodebuild.json bundle exec xcpretty -f `bundle exec xcpretty-json-formatter`
else
  xcodebuild -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" analyze build-for-testing | tee "xcode_build.log" | XCPRETTY_JSON_FILE_OUTPUT=xcodebuild.json bundle exec xcpretty -f `bundle exec xcpretty-json-formatter`
fi
