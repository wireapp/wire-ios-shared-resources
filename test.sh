#! /bin/bash

set -eu -o pipefail

bundle install --path ~/.gem

if [ "$IS_UI_PROJECT" -eq "1" ]; 
then
	# Running tests from .xctestrun file seems broken, falling back to regular command
	xcodebuild -destination "${DESTINATION}" -workspace "${WORKSPACE}" -scheme "${SCHEME}" -derivedDataPath DerivedData test-without-building | tee xcode_test.log | bundle exec xcpretty -r junit --output junit/tests.xml
else
	XCRUN_FILES=( DerivedData/Build/Products/*.xctestrun )

	xcodebuild -enableCodeCoverage YES -destination "${DESTINATION}" -xctestrun "${XCRUN_FILES[0]}" test-without-building | tee xcode_test.log | bundle exec xcpretty -r junit --output junit/tests.xml
fi


