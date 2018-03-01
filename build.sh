#! /bin/bash

set -euf -o pipefail

arguments=( -scheme "${SCHEME}"  )
arguments+=( -destination "${DESTINATION}" )
arguments+=( -derivedDataPath DerivedData )
arguments+=( -enableCodeCoverage YES )

if [ "$IS_UI_PROJECT" -eq "1" ]; 
then
	arguments+=( -workspace "${WORKSPACE}" ) # We need to append workspace argument for UI project
else
	arguments+=( analyze ) # Analyze only frameworks - takes a long time on UI project
fi

arguments+=( build-for-testing )

xcodebuild "${arguments[@]}" | XCPRETTY_JSON_FILE_OUTPUT=build/xcodebuild.json bundle exec xcpretty -f `bundle exec xcpretty-json-formatter`