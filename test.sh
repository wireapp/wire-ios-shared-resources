#! /bin/sh

set -euf -o pipefail

xcodebuild -scheme "${SCHEME}" test-without-building -enableCodeCoverage YES -destination "${DESTINATION}" | tee $CIRCLE_ARTIFACTS/xcode_test.log | xcpretty -r junit --output $CIRCLE_TEST_REPORTS/junit/tests.xml