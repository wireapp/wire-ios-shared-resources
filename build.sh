#! /bin/bash

set -euf -o pipefail

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
  xcodebuild -workspace "Wire-iOS.xcworkspace" -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" analyze build-for-testing | tee "${CIRCLE_ARTIFACTS}/xcode_build.log" | XCPRETTY_JSON_FILE_OUTPUT=xcodebuild.json xcpretty -f `bundle exec xcpretty-json-formatter`
else
  xcodebuild -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" analyze build-for-testing | tee "${CIRCLE_ARTIFACTS}/xcode_build.log" | XCPRETTY_JSON_FILE_OUTPUT=xcodebuild.json xcpretty -f `bundle exec xcpretty-json-formatter`
fi
