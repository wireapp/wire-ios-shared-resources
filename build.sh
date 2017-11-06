#! /bin/bash

set -euf -o pipefail

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
  xcodebuild -workspace "Wire-iOS.xcworkspace" -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" analyze build-for-testing | tee "${CIRCLE_ARTIFACTS}/xcode_build.log" | xcpretty
else
  xcodebuild -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" analyze build-for-testing | tee "${CIRCLE_ARTIFACTS}/xcode_build.log" | xcpretty
fi
