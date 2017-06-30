#! /bin/sh

set -euf -o pipefail

echo "Downloading ${DEPENDENCIES_BASE_URL}/Gemfile"
curl -O "${DEPENDENCIES_BASE_URL}/Gemfile"

echo "Downloading ${DEPENDENCIES_BASE_URL}/Gemfile.lock"
curl -O "${DEPENDENCIES_BASE_URL}/Gemfile.lock"

bundle install --path ~/.gem
carthage bootstrap
