pipeline {
    agent any
    triggers {
        pollSCM('H/4 * * * *')
    }
    options {
        ansiColor('xterm')
    }
    environment {
        // For uploading to AppStore
        KEY_ID = credentials('app_store_connect_api_key_id')
        KEY_ID_ISSUER = credentials('app_store_connect_issuer_id')
        KEY_FILE_PATH = credentials('app_store_connect_api_keypath')

        // For provisioning profiles update
        APPSTORE_CONNECT_TEAM_ID = credentials('appstore_connect_team_id')
	
        // Most fool-proof way to make sure rbenv and ruby works fine
        PATH = '/Users/ci/.rbenv/shims:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin'

        // Turn off annoying update checks
        FASTLANE_SKIP_UPDATE_CHECK = 'YES'
    }

    parameters {
        choice(
            choices: ['Playground',
             'Development',
             'Internal',
             'AVS',
             'RC',
             'Bund_RC_1',
             'Bund_RC_3',
             'Bund_AppStore_1',
             'Bund_AppStore_3',
             'Beta'],
            description: 'Type of build',
            name: 'BUILD_TYPE'
        )
        choice(
            choices: ['Release', 'Debug'],
            description: 'Configuration of build',
            name: 'BUILD_CONFIGURATION'
        )
        booleanParam(name: 'REGISTER_DEVICES',
                     defaultValue: true,
                     description: 'Renew all provisioning profiles of all build types')
    }


    stages {
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
                            cd wire-ios-build-assets; bundle install --path ~/.gem
                        """
                    }
                }
            }
        }

    stage('Generate Provisioning Profiles') {
        when {
            anyOf {
                changeset "**/devices.txt"
                triggeredBy cause: "UserIdCause"
                branch "master"
            }
        }
        steps {
            withEnv([
                "FASTLANE_TEAM_ID=${APPSTORE_CONNECT_TEAM_ID}"
            ]) {
                script {
                    if (params.REGISTER_DEVICES) {
                        sh """#!/bin/bash -l
                        cd wire-ios-build-assets; bundle exec fastlane devices
                        """
                    } else {
                        sh """#!/bin/bash -l
                            cd wire-ios-build-assets; bundle exec fastlane profiles build_type:${BUILD_TYPE} configuration:${BUILD_CONFIGURATION} force:
                        """
                    }
                }
            }

        }
        post {
            always {
                archiveArtifacts artifacts: 'wire-ios-build-assets/*.mobileprovision', fingerprint: true
            }
        }
    }
    }
}
