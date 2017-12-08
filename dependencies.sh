#! /bin/bash

set -euf -o pipefail

if [ "$CIRCLE_PROJECT_REPONAME" == "wire-ios" ]; 
then
    # Preheat cocoapods main repo
    echo "Downloading CocoaPods main repo"
    bash <(curl -s "https://cocoapods-specs.circleci.com/fetch-cocoapods-repo-from-s3.sh")
    echo "Running setup.sh"
    ./setup.sh
else
    bundle install --path ~/.gem
    carthage bootstrap --platform iOS --cache-builds
fi

