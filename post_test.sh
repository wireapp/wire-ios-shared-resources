#! /bin/sh

bundle exec slather coverage --html --scheme "${SCHEME}" --output-directory $CIRCLE_TEST_REPORTS/code_coverage/ $PROJECT

bundle exec danger

mkdir "${CIRCLE_ARTIFACTS}/scripts"
cp *.sh "${CIRCLE_ARTIFACTS}/scripts"
cp "${HOME}/.circlerc" "${CIRCLE_ARTIFACTS}/scripts"
