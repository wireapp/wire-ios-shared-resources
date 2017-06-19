#! /bin/sh

set -euf -o pipefail

bundle install --path ~/.gem
carthage bootstrap
curl -O https://raw.githubusercontent.com/wireapp/wire-ios-shared-resources/master/Dangerfile
