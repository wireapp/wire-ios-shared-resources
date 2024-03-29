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
        GITHUB_AVS_ACCESS_TOKEN = credentials('github-api-token')

        // Most fool-proof way to make sure rbenv and ruby works fine
        PATH = "/Users/ci/.rbenv/shims:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
    }
    parameters {
        // Choose which framework to release
        choice(
            choices: ['wire-ios-notification-engine', 'wire-ios-canvas', 'wire-ios-cryptobox', 'wire-ios-data-model', 'wire-ios-images', 'wire-ios-link-preview', 'wire-ios-mocktransport', 'wire-ios-protos', 'wire-ios-request-strategy', 'wire-ios-scripting-helpers', 'wire-ios-share-engine', 'wire-ios-sync-engine', 'wire-ios-system', 'wire-ios-testing', 'wire-ios-transport', 'wire-ios-utilities', 'wire-ios-ziphy'], 
            description: 'The framework for which you want to release a new version.', 
            name: 'BOT_FRAMEWORK'
        )
        // Choose which release type
        choice(
            choices: ['patch', 'minor', 'major'], 
            description: 'Type of release, as defined in: http://semver.org/', 
            name: 'BOT_TYPE'
        )
    }

    stages {
        stage('Release') {
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
                withEnv(['GIT_BRANCH=develop']) {
                    // Running fastlane script to release the framework
                    sh """#!/bin/bash -l
                        curl -O ${DEPENDENCIES_BASE_URL}/Gemfile
                        curl -O ${DEPENDENCIES_BASE_URL}/Gemfile.lock
                        mkdir -p fastlane
                        curl -o fastlane/Fastfile ${DEPENDENCIES_BASE_URL}/Fastlane/Frameworks

                        bundle install --path ~/.gem
                        bundle exec fastlane release type:${BOT_TYPE}
                    """
                }
            }
        }
    }
}

