pipeline {
    agent any
    tools {nodejs "node-v19.5.0"}
    options {
        ansiColor('xterm')
    }
    environment { 
        // This is where the misc configuration files (e.g. Gemfile) are fetched from. When testing swap out the branch
        DEPENDENCIES_BASE_URL = "https://raw.githubusercontent.com/wireapp/wire-ios-shared-resources/master"
        // For command line tools to be able to access API we set there env vars to values from Jenkins credentials store
        GITHUB_TOKEN = credentials('github-api-token')
        GITHUB_ACCESS_TOKEN = credentials('github-api-token')
        GITHUB_AVS_ACCESS_TOKEN = credentials('github-api-token')
        
        // For uploading builds to S3 bucket
        AWS_ACCESS_KEY_ID = credentials('s3_access_key')
        AWS_SECRET_ACCESS_KEY = credentials('s3_secret_access_key')

        // For uploading to AppStore
        KEY_ID = credentials('app_store_connect_api_key_id')
        KEY_ID_ISSUER = credentials('app_store_connect_issuer_id')
        KEY_FILE_PATH = credentials('app_store_connect_api_keypath')
        APPSTORE_CONNECT_TEAM_ID = credentials('appstore_connect_team_id')

        // Most fool-proof way to make sure rbenv and ruby works fine
        PATH = "/Users/ci/.rbenv/shims:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"

        // Turn off annoying update checks
        FASTLANE_SKIP_UPDATE_CHECK = "YES"

        // This will be set to app build number
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
        BUILD_TYPE="Beta"

        // Repository from which to fetch custom AVS binary
        AVS_REPO = "wireapp/avs-ios-binaries-appstore"
    
        // DATADOG API
        DATADOG_API_KEY = credentials('datadog_api_key')
    }
    
    parameters {
        string(
            defaultValue: "develop", 
            description: 'Branch to build from.', 
            name: 'branch_to_build'
        )
        string(
            defaultValue: "", 
            description: 'Overridden build number.', 
            name: 'build_number_override'
        )
        string(
            defaultValue: "", 
            description: 'Produces changelog from all commits added since this commit.', 
            name: 'last_commit_for_changelog'
        )
        choice(
            choices: ["13.2.1"],
            description: 'Xcode version to build with.',
            name: "xcode_version"
        )
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
                    userRemoteConfigs: [[url: "git@github.com:wireapp/wire-ios-mono.git"]]
                ])

            }
        }

        stage('build-assets & Gems') {
            parallel {
                stage('checkout wire-ios-build-assets') {
                    steps {
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
                    }
                }

                stage('Gems') {
                    steps {
                        sh """#!/bin/bash -l
                            curl -O ${DEPENDENCIES_BASE_URL}/Gemfile
                            curl -O ${DEPENDENCIES_BASE_URL}/Gemfile.lock
                            bundle install --path ~/.gem

                            echo "setting DEVELOPER_DIR to ${xcode_version}"
                            export DEVELOPER_DIR=/Applications/Xcode_${xcode_version}.app/Contents/Developer
                        """
                    }
                }
            }
        }

        stage('fastlane prepare') {
            steps {
                sh """#!/bin/bash -l
                    bundle exec fastlane prepare \
                    build_number:${BUILD_NUMBER} \
                    build_type:${BUILD_TYPE} \
                    xcode_version:${xcode_version}
                """

                // Make sure that all subsequent steps see the branch from main project, not from build assets
                script {
                    GIT_BRANCH = "${branch_to_build}"
                }
            }
        }

        stage('Build for release') {
            steps {
                sh """#!/bin/bash -l
                    bundle exec fastlane build_for_release \
                    build_number:${BUILD_NUMBER} \
                    build_type:${BUILD_TYPE} \
                    xcode_version:${xcode_version}
                """
            }
        }
        stage("Upload results & code converage") {
            parallel {
                stage("Upload to S3") {
                    steps {
                        sh """#!/bin/bash -l
                            bundle exec fastlane upload_s3 \
                            build_number:${BUILD_NUMBER} \
                            build_type:${BUILD_TYPE}
                        """
                    }
                }
                stage ("Upload to TestFlight") {
                    steps {
                        withEnv([
                            "FASTLANE_TEAM_ID=${APPSTORE_CONNECT_TEAM_ID}"
                        ]) {
                            sh """#!/bin/bash -l
                                bundle exec fastlane upload_testflight build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE}
                            """
                        }
                    }
                }
            
                stage('Upload dSyms to Datadog') {
                    steps {
                        sh """#!/bin/bash -l
                               bundle exec fastlane upload_dsyms_datadog build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE}
                        """                      
                    }
                }

            }
        }
    }
    post {
        always {

            junit testResults: "test/*.junit", allowEmptyResults: true
        }
    }
}
