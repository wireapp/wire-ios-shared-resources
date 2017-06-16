#! /bin/sh

set -euf -o pipefail

xcodebuild -scheme "${SCHEME}" build-for-testing -enableCodeCoverage YES -destination "${DESTINATION}" | tee "${CIRCLE_ARTIFACTS}/xcode_build.log" | xcpretty
