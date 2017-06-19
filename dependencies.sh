#! /bin/sh

set -euf -o pipefail

bundle install --path ~/.gem
carthage bootstrap
