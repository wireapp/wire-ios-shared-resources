#! /bin/bash

echo "export DESTINATION='platform=iOS Simulator,name=iPhone 7,OS=11.0.1'" >> $BASH_ENV

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
	echo "export SCHEME='Wire-iOS'" >> $BASH_ENV
else
	echo "export SCHEME=\"$(xcodebuild -list | awk '/^[[:space:]]*Schemes:/{getline; print $0;}' | sed 's/^ *//;s/ *$//')\"" >> $BASH_ENV
fi

ALL_PROJECTS=( *.xcodeproj )
echo "export PROJECT='${ALL_PROJECTS[0]}'" >> $BASH_ENV
