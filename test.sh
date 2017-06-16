#! /bin/sh

set -euf -o pipefail

DESTINATION="platform=iOS Simulator,name=iPhone 6,OS=10.3"

SCHEME=$(xcodebuild -list | awk '/^[[:space:]]*Schemes:/{getline; print $0;}' | tr -d '[:space:]') 

xcodebuild -scheme "${SCHEME}" test-without-building -enableCodeCoverage YES -destination "${DESTINATION}" | tee $CIRCLE_ARTIFACTS/xcode_test.log | xcpretty -r junit --output $CIRCLE_TEST_REPORTS/junit/tests.xml