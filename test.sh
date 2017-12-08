#! /bin/bash

set -euf -o pipefail

bundle install --path ~/.gem

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
  xcodebuild -workspace "Wire-iOS.xcworkspace" -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" -derivedDataPath DerivedData test-without-building | tee "xcode_test.log" | bundle exec xcpretty -r junit --output junit/tests.xml
else
  xcodebuild -enableCodeCoverage YES -destination "${DESTINATION}" -xctestrun DerivedData/Build/Products/*.xctestrun test-without-building | tee xcode_test.log | bundle exec xcpretty -r junit --output junit/tests.xml
fi
