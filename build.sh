#! /bin/bash

set -euf -o pipefail

arguments=( -scheme "${SCHEME}"  )
arguments+=( -destination "${DESTINATION}" )
arguments+=( -derivedDataPath DerivedData )

if [ "$IS_UI_PROJECT" -eq "1" ]; 
then
	arguments+=( -workspace "${WORKSPACE}" ) # We need to append workspace argument for UI project
else
	arguments+=( -enableCodeCoverage YES ) # Track code coverage in frameworks
	arguments+=( analyze ) # Also analyze frameworks
fi

arguments+=( build-for-testing )

xcodebuild "${arguments[@]}" | XCPRETTY_JSON_FILE_OUTPUT=xcodebuild.json bundle exec xcpretty -f `bundle exec xcpretty-json-formatter`