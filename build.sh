#! /bin/sh

set -euf -o pipefail

xcodebuild -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" build-for-testing | tee "${CIRCLE_ARTIFACTS}/xcode_build.log" | xcpretty
