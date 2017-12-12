#! /bin/bash

set -eu -o pipefail

bundle install --path ~/.gem

XCRUN_FILES=( DerivedData/Build/Products/*.xctestrun )

xcodebuild -enableCodeCoverage YES -destination "${DESTINATION}" -xctestrun "${XCRUN_FILES[0]}" test-without-building | tee xcode_test.log | bundle exec xcpretty -r junit --output junit/tests.xml
