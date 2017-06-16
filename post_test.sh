#! /bin/sh

set -euf -o pipefail

bundle exec slather coverage --html --scheme "${SCHEME}" --output-directory $CIRCLE_TEST_REPORTS/code_coverage/ $PROJECT