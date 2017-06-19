#! /bin/sh

set -euf -o pipefail

xcodebuild -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" test-without-building | tee $CIRCLE_ARTIFACTS/xcode_test.log | xcpretty -r junit --output $CIRCLE_TEST_REPORTS/junit/tests.xml