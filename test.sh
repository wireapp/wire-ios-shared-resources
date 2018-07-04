#! /bin/bash

set -eu -o pipefail

bundle install --path ~/.gem

arguments=( -scheme "${SCHEME}"  )
arguments+=( -destination "${DESTINATION}" )
arguments+=( -derivedDataPath DerivedData )
arguments+=( -enableCodeCoverage YES )
arguments+=( test-without-building )

mkdir -p build/junit

xcodebuild "${arguments[@]}" | tee build/xcode_test.log | bundle exec xcpretty -r junit --output build/junit/tests.xml