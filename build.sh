#! /bin/sh

set -euf -o pipefail

DESTINATION="platform=iOS Simulator,name=iPhone 6,OS=10.3"

SCHEME=$(xcodebuild -list | awk '/^[[:space:]]*Schemes:/{getline; print $0;}' | tr -d '[:space:]') 

xcodebuild -scheme "${SCHEME}" build-for-testing -enableCodeCoverage YES -destination "${DESTINATION}" | tee $CIRCLE_ARTIFACTS/xcode_build.log | xcpretty