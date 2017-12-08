#! /bin/bash

set -euf -o pipefail

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
  xcodebuild -workspace "Wire-iOS.xcworkspace" -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" -derivedDataPath DerivedData test-without-building | tee "xcode_test.log" | xcpretty -r junit --output junit/tests.xml
else
  xcodebuild -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" -derivedDataPath DerivedData test-without-building | tee xcode_test.log | xcpretty -r junit --output junit/tests.xml
fi
