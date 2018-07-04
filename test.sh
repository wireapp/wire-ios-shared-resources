#! /bin/bash

set -eu -o pipefail

bundle install --path ~/.gem

arguments=( -scheme "${SCHEME}"  )
arguments+=( -destination "${DESTINATION}" )
arguments+=( -derivedDataPath DerivedData )

if [ "$IS_UI_PROJECT" -eq "1" ]; 
then
	arguments+=( -project "${PROJECT}" ) # We need to append workspace argument for UI project
fi

arguments+=( -enableCodeCoverage YES )
arguments+=( test-without-building )

mkdir -p build/junit

xcodebuild "${arguments[@]}" | tee build/xcode_test.log | bundle exec xcpretty -r junit --output build/junit/tests.xml