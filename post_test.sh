#! /bin/sh

EXIT=0
# bundle exec slather coverage --html --scheme "${SCHEME}" --output-directory $CIRCLE_TEST_REPORTS/code_coverage/ $PROJECT

echo "Downloading ${DEPENDENCIES_BASE_URL}/Dangerfile"
curl -O "${DEPENDENCIES_BASE_URL}/Dangerfile" || EXIT=$?
bundle exec danger || EXIT=$?

echo "Creating directory for all scripts"
mkdir "${CIRCLE_ARTIFACTS}/scripts" || EXIT=$?

echo "Copying all shell scripts"
cp *.sh "${CIRCLE_ARTIFACTS}/scripts" || EXIT=$?

# Fail if any of the commands had exit status != 0
exit $EXIT
