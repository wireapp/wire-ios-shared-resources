#! /bin/bash

echo "export DESTINATION='platform=iOS Simulator,name=iPhone 7,OS=11.0.1'" >> $BASH_ENV

curl -O "${DEPENDENCIES_BASE_URL}/.ruby-version"
curl -O "${DEPENDENCIES_BASE_URL}/dependencies.sh"
curl -O "${DEPENDENCIES_BASE_URL}/build.sh"
curl -O "${DEPENDENCIES_BASE_URL}/test.sh"
curl -O "${DEPENDENCIES_BASE_URL}/post_test.sh"

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
	echo "export IS_UI_PROJECT=1" >> $BASH_ENV
	echo "export SCHEME='Wire-iOS'" >> $BASH_ENV
	echo "export WORKSPACE='Wire-iOS.xcworkspace'" >> $BASH_ENV
else
	echo "export IS_UI_PROJECT=0" >> $BASH_ENV
	curl -O "${DEPENDENCIES_BASE_URL}/Gemfile"
	curl -O "${DEPENDENCIES_BASE_URL}/Gemfile.lock"
	echo "export SCHEME=\"$(xcodebuild -list | awk '/^[[:space:]]*Schemes:/{getline; print $0;}' | sed 's/^ *//;s/ *$//')\"" >> $BASH_ENV
fi

ALL_PROJECTS=( *.xcodeproj )
echo "export PROJECT='${ALL_PROJECTS[0]}'" >> $BASH_ENV
