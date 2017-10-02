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
    echo "Downloading ${DEPENDENCIES_BASE_URL}/Gemfile"
    curl -O "${DEPENDENCIES_BASE_URL}/Gemfile"

    echo "Downloading ${DEPENDENCIES_BASE_URL}/Gemfile.lock"
    curl -O "${DEPENDENCIES_BASE_URL}/Gemfile.lock"

    echo "Downloading ${DEPENDENCIES_BASE_URL}/Romefile"
    curl -O "${DEPENDENCIES_BASE_URL}/Romefile"

    bundle install --path ~/.gem

    echo "Installing Rome"
    brew install blender/homebrew-tap/rome

    rome download --platform iOS # download missing frameworks (or copy from local cache)
    rome list --missing --platform ios | awk '{print $1}' | xargs carthage bootstrap --platform iOS --cache-builds # list what is missing and update/build if needed
    rome list --missing --platform ios | awk '{print $1}' | xargs rome upload --platform ios # upload what is missing
fi

