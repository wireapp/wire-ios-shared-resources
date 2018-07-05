#! /bin/bash

set -euf -o pipefail

bundle install --path ~/.gem

if [ "$IS_UI_PROJECT" -eq "1" ]; 
then
    echo "Running setup.sh"
    ./setup.sh
else
    if [ -e "Cartfile" ]; then
        carthage bootstrap --platform iOS --cache-builds
    else
        echo "No Cartfile found, skipping bootstrap"
    fi 
fi

