#! /bin/sh

set -euf -o pipefail

SCHEME=$(xcodebuild -list | awk '/^[[:space:]]*Schemes:/{getline; print $0;}' | tr -d '[:space:]') 

xcodebuild -scheme "${SCHEME}" build-for-testing -enableCodeCoverage YES -destination "${DESTINATION}" | tee $CIRCLE_ARTIFACTS/xcode_build.log | xcpretty