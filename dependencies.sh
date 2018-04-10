#! /bin/bash

set -euf -o pipefail

if [ "$IS_UI_PROJECT" -eq "1" ]; 
then
    # Preheat cocoapods main repo
    echo "Downloading CocoaPods main repo"
    bash <(curl -s "https://cocoapods-specs.circleci.com/fetch-cocoapods-repo-from-s3.sh")
    echo "Running setup.sh"
    ./setup.sh
else
    bundle install --path ~/.gem
    if [ -e "Cartfile" ]; then
        carthage bootstrap --platform iOS --cache-builds
    else
        echo "No Cartfile found, skipping bootstrap"
    fi 
fi

