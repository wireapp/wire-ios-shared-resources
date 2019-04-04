pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    environment { 
        // This is where the misc configuration files (e.g. Gemfile) are fetched from. When testing swap out the branch
        DEPENDENCIES_BASE_URL = "https://raw.githubusercontent.com/wireapp/wire-ios-shared-resources/master"
        // For command line tools to be able to access API we set there env vars to values from Jenkins credentials store
        GITHUB_TOKEN = credentials('github-api-token')
        GITHUB_ACCESS_TOKEN = credentials('github-api-token')

        // For uploading builds to S3 bucket
        AWS_ACCESS_KEY_ID = credentials('s3_access_key')
        AWS_SECRET_ACCESS_KEY = credentials('s3_secret_access_key')

        // For uploading to AppStore
        APPSTORE_CONNECT_USER = credentials('appstore_connect_username')
        APPSTORE_CONNECT_PASSWORD = credentials('appstore_connect_password')

        // Most fool-proof way to make sure rbenv and ruby works fine
        PATH = "/Users/ci/.rbenv/shims:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"

        // Turn off annoying update checks
        FASTLANE_SKIP_UPDATE_CHECK = "YES"

        // This will be set to app build number
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
        BUILD_TYPE = "AppStore"

        // Repository from which to fetch custom AVS binary
        AVS_REPO = "wireapp/avs-ios-binaries-appstore"
    }
    parameters {
        string(defaultValue: "develop", description: 'Branch to use', name: 'branch_to_build')
        string(defaultValue: "", description: 'Override build number with', name: 'build_number_override')
    }

    stages {
        stage('Checkout') {
            steps {
                // Let's check if we are overriding the app build number 
                script {
                    if ("${build_number_override}" != '') {
                        BUILD_NUMBER = "${build_number_override}"
                    }
                }
                // Checking out the correct repository. This is dynamic because we have one job that can build multiple frameworks
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/${branch_to_build}']], // Checks out specified branch
                    extensions: [
                        [$class: 'LocalBranch', localBranch: '**'], // Unless this is specified, it simply checks out by commit SHA with no branch information
                        [$class: 'CleanBeforeCheckout'], // Resets untracked files, just to make sure we are clean
                        [$class: 'CloneOption', timeout: 60] // Timeout after 1 hour
                    ],
                    userRemoteConfigs: [[url: "git@github.com:wireapp/wire-ios.git"]]
                ])
                dir("wire-ios-build-assets") {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/master']], // Checks out specified branch
                        extensions: [
                            [$class: 'LocalBranch', localBranch: '**'], // Unless this is specified, it simply checks out by commit SHA with no branch information
                            [$class: 'CleanBeforeCheckout'] // Resets untracked files, just to make sure we are clean
                        ],
                        userRemoteConfigs: [[url: "git@github.com:wireapp/wire-ios-build-assets.git"]]
                    ])
                }

                sh """#!/bin/bash -l
                    curl -O ${DEPENDENCIES_BASE_URL}/Gemfile
                    curl -O ${DEPENDENCIES_BASE_URL}/Gemfile.lock

                    bundle install --path ~/.gem
                    bundle exec fastlane prepare build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE}
                """
                // Make sure that all subsequent steps see the branch from main project, not from build assets
                script {
                    GIT_BRANCH = "${branch_to_build}"
                }
            }
        }
        stage('Build for AppStore') {
            steps {
                sh """#!/bin/bash -l
                    bundle exec fastlane build_for_release build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE}
                """
            }
        }
        stage("Upload results") {
            parallel {
                stage("Upload to S3") {
                    steps {
                        sh """#!/bin/bash -l
                            bundle exec fastlane upload_s3 build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE}
                        """
                    }
                }
                stage('Upload to AppStore') {
                    steps {
                        withEnv([
                            "FASTLANE_USER=${APPSTORE_CONNECT_USER}",
                            "FASTLANE_PASSWORD=${APPSTORE_CONNECT_PASSWORD}"
                        ]) {
                            sh """#!/bin/bash -l
                                bundle exec fastlane upload_app_store build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE}
                            """
                        }
                    }
                }
                stage('Upload to Hockey') {
                    steps {
                        sh """#!/bin/bash -l
                            bundle exec fastlane upload_hockey_appstore build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE}
                        """
                    }
                }
            }
        }
    }
}

