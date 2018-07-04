#! /bin/bash

set -euf -o pipefail

if [ "$IS_UI_PROJECT" -eq "1" ]; 
then
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

