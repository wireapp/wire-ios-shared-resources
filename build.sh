#! /bin/bash

set -euf -o pipefail

echo "HOME = ${HOME}
echo "Sourcing .circlerc, `wc -l $HOME/.circlerc` lines long"
source $HOME/.circlerc

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
  xcodebuild -workspace "Wire-iOS.xcworkspace" -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" build-for-testing | tee "${CIRCLE_ARTIFACTS}/xcode_build.log" | xcpretty
else
  xcodebuild -scheme "${SCHEME}" -enableCodeCoverage YES -destination "${DESTINATION}" build-for-testing | tee "${CIRCLE_ARTIFACTS}/xcode_build.log" | xcpretty
fi
