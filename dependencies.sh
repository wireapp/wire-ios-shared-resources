#! /bin/sh

set -euf -o pipefail

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios"]; 
then
	# Preheat cocoapods main repo
	echo "Downloading CocoaPods main repo"
    bash <(curl -s "https://cocoapods-specs.circleci.com/fetch-cocoapods-repo-from-s3.sh")
    echo "Running setup.sh"
    ./setup.sh
else
	echo "Downloading ${DEPENDENCIES_BASE_URL}/Gemfile"
	curl -O "${DEPENDENCIES_BASE_URL}/Gemfile"

	echo "Downloading ${DEPENDENCIES_BASE_URL}/Gemfile.lock"
	curl -O "${DEPENDENCIES_BASE_URL}/Gemfile.lock"

	bundle install --path ~/.gem
	carthage bootstrap
fi

