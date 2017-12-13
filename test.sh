#! /bin/bash

set -eu -o pipefail

bundle install --path ~/.gem

arguments=( -scheme "${SCHEME}"  )
arguments+=( -destination "${DESTINATION}" )
arguments+=( -derivedDataPath DerivedData )

if [ "$IS_UI_PROJECT" -eq "1" ]; 
then
	arguments+=( -workspace "${WORKSPACE}" ) # We need to append workspace argument for UI project
else
	arguments+=( -enableCodeCoverage YES ) # Track code coverage in frameworks
fi

arguments+=( test-without-building )

xcodebuild "${arguments[@]}" | tee xcode_test.log | bundle exec xcpretty -r junit --output junit/tests.xml