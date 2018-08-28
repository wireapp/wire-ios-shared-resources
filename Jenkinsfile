pipeline {
    agent { 
        // This job will be run on all machines that have this label set up
        label 'frameworks'
    }
    environment { 
        // This is where the misc configuration files (e.g. Gemfile) are fetched from. When testing swap out the branch
        DEPENDENCIES_BASE_URL = "https://raw.githubusercontent.com/wireapp/wire-ios-shared-resources/master"
        // For command line tools to be able to access API we set there env vars to values from Jenkins credentials store
        GITHUB_TOKEN = credentials('github-api-token')
        GITHUB_ACCESS_TOKEN = credentials('github-api-token')
        // Most fool-proof way to make sure rbenv and ruby works fine
        PATH = "/Users/ci/.rbenv/shims:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
        // When tests are finished we want to upload test coverage to Codecov.io. This is needed to associate it with the right project.
        CODECOV_TOKEN = credentials("codecov-${params.BOT_FRAMEWORK}")
    }
    parameters {
        // Choose which framework to release
        choice(
            choices: ['wire-ios-canvas', 'wire-ios-cryptobox', 'wire-ios-data-model', 'wire-ios-images', 'wire-ios-link-preview', 'wire-ios-mocktransport', 'wire-ios-protos', 'wire-ios-request-strategy', 'wire-ios-scripting-helpers', 'wire-ios-share-engine', 'wire-ios-sync-engine', 'wire-ios-system', 'wire-ios-testing', 'wire-ios-transport', 'wire-ios-utilities', 'wire-ios-ziphy'], 
            description: 'The framework for which you want to release a new version.', 
            name: 'BOT_FRAMEWORK'
        )
        // Choose which release type
        choice(
            choices: ['patch', 'minor', 'major'], 
            description: 'Type of release, as defined in: http://semver.org/', 
            name: 'BOT_TYPE'
        )
        // Choose whether to skip tests
        booleanParam(
            defaultValue: true, 
            description: 'Whether to run tests. If unchecked, will skip tests.', 
            name: 'RUN_TESTS'
        )
    }

    // Pipeline consists of several stages
    stages {
        stage('Prepare') {
            steps {
                // Checking out the correct repository. This is dynamic because we have one job that can build multiple frameworks
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/develop']], // Checks out develop branch
                    extensions: [
                        [$class: 'LocalBranch', localBranch: '**'], // Unless this is specified, it simply checks out by commit SHA with no branch information
                        [$class: 'CleanBeforeCheckout'] // Resets untracked files, just to make sure we are clean
                    ],
                    userRemoteConfigs: [[url: "git@github.com:wireapp/${BOT_FRAMEWORK}.git"]] // Dynamically create repo URL from parameters
                ])
                // Fetch dependencies used by Ruby tools from a shared place
                sh "curl -O ${DEPENDENCIES_BASE_URL}/Gemfile"
                sh "curl -O ${DEPENDENCIES_BASE_URL}/Gemfile.lock"
            }
        }
        stage('Build') {
            when {
                // No need to build if we are skipping tests
                expression { params.RUN_TESTS == true }
            }
            steps {
                // Launches fastlane script for building
                sh ''' #!/bin/bash -l
                    bundle install --path ~/.gem
                    bundle exec fastlane build
                '''
            }
        }
        
        stage('Test') {
            when {
                // Skipped if we don't want to run tests
                expression { params.RUN_TESTS == true }
            }
            steps {
                // Launches fastlane test and post_test scripts
                sh ''' #!/bin/bash -l
                    bundle exec fastlane test
                    bundle exec fastlane post_test
                '''
            }
        }
        stage('Release') {
            steps {
                // This env var points to the branch where this Jenkins file is in, not the framework project we just checked out and scripts get messed up. We know we are in develop now!
                withEnv(['GIT_BRANCH=develop']) {
                    // Running fastlane script to release the framework
                    // In addition, running post_test task to upload (same) test coverage after making a commit with version increase
                    sh """#!/bin/bash -l
                        bundle install --path ~/.gem
                        bundle exec fastlane release type:${BOT_TYPE}
                        bundle exec fastlane post_test
                    """
                }
            }
        }
    }
    post {
        always {
            // Save test results and artifacts no matter what was the result of pipelines
            archiveResults(params.RUN_TESTS)
        }
    }
}

def archiveResults(boolean runningTests) {
    // Build log
    archiveArtifacts artifacts: 'build/*.log', allowEmptyArchive: true
    // Test log
    archiveArtifacts artifacts: 'test/*.log', allowEmptyArchive: true
    if (runningTests) {
        // Upload test results if we have them
        junit 'test/*.xml'
    }
}
