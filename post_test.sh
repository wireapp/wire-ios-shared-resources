#! /bin/sh

EXIT=0
# bundle exec slather coverage --html --scheme "${SCHEME}" --output-directory $CIRCLE_TEST_REPORTS/code_coverage/ $PROJECT

bundle exec danger || EXIT=$?

mkdir "${CIRCLE_ARTIFACTS}/scripts" || EXIT=$?
cp *.sh "${CIRCLE_ARTIFACTS}/scripts" || EXIT=$?
cp "${HOME}/.circlerc" "${CIRCLE_ARTIFACTS}/scripts" || EXIT=$?

# Fail if any of the commands had exit status != 0
exit $EXIT
