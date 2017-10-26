#! /bin/bash

echo "export DESTINATION='platform=iOS Simulator,name=iPhone 7,OS=11.0'" >> env-vars

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
	echo "export SCHEME='Wire-iOS'" >> env-vars
else
	echo "export SCHEME=\"$(xcodebuild -list | awk '/^[[:space:]]*Schemes:/{getline; print $0;}' | sed 's/^ *//;s/ *$//')\"" >> env-vars
fi

ALL_PROJECTS=( *.xcodeproj )
echo "export PROJECT='${ALL_PROJECTS[0]}'" >> env-vars
