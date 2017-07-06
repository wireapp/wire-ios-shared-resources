#! /bin/bash

echo "export DESTINATION='platform=iOS Simulator,name=iPhone 6,OS=10.2'" >> $HOME/.circlerc

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios"]; 
then
	echo "export SCHEME=Wire-iOS" >> $HOME/.circlerc
else
	echo "export SCHEME=\"$(xcodebuild -list | awk '/^[[:space:]]*Schemes:/{getline; print $0;}' | sed 's/^ *//;s/ *$//')\"" >> $HOME/.circlerc
fi

ALL_PROJECTS=( *.xcodeproj )
echo "export PROJECT='${ALL_PROJECTS[0]}'" >> $HOME/.circlerc
