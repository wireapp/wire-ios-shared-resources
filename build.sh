#! /bin/bash

set -euf -o pipefail

echo "Sourcing env-vars"
tail -n 3 env-vars
source env-vars

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
  xcodebuild -workspace "Wire-iOS.xcworkspace" -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" build-for-testing | tee "${CIRCLE_ARTIFACTS}/xcode_build.log" | xcpretty
else
  xcodebuild -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" build-for-testing | tee "${CIRCLE_ARTIFACTS}/xcode_build.log" | xcpretty
fi
