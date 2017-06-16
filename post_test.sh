#! /bin/sh

set -euf -o pipefail

SCHEME=$(xcodebuild -list | awk '/^[[:space:]]*Schemes:/{getline; print $0;}' | tr -d '[:space:]') 

bundle exec slather coverage --html --scheme "${SCHEME}" --output-directory $CIRCLE_TEST_REPORTS/code_coverage/ *.xcodeproj