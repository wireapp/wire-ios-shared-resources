pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    triggers { 
        pollSCM('H/2 * * * *') 
    }
    environment {
        BRANCH = ""
    }
    parameters {
        string(defaultValue: "12.4", description: 'XCode version to use (12.4)', name: 'xcode_version')
    }
    stages {
        stage('Checkout') {
            steps {
                script {
                    def scmVars = checkout([
                        $class: 'GitSCM',
                        branches: [[name: "origin/release/**"]],
                        extensions: [
                            [$class: 'AuthorInChangelog'],
                            [$class: 'CloneOption', depth: 0, honorRefspec: true, noTags: true, reference: ''],
                            [$class: 'CheckoutOption', timeout: 30],
                            [$class: 'LocalBranch', localBranch: '**'], // Unless this is specified, it simply checks out by commit SHA with no branch information
                            [$class: 'CleanBeforeCheckout'] // Resets untracked files, just to make sure we are clean
                        ],
                        userRemoteConfigs: [[url: "git@github.com:wireapp/wire-ios.git"]]
                    ])

                    LAST_COMMIT = scmVars.GIT_PREVIOUS_COMMIT ?: ""
                    BRANCH = scmVars.GIT_LOCAL_BRANCH
                }
            }
        }
        stage ("Trigger build") {
            steps {
                build(
                    job: 'client-ios-build-pipeline', 
                    parameters: [
                        string(name: 'xcode_version', value: xcode_version), 
                        string(name: 'branch_to_build', value: BRANCH), 
                        string(name: 'BUILD_TYPE', value: 'RC'), 
                        string(name: 'build_number_override', value: env.BUILD_NUMBER),
                        string(name: 'last_commit_for_changelog', value: LAST_COMMIT)
                    ]
                )
                build(
                    job: 'client-ios-build-appstore-pipeline', 
                    parameters: [
                        string(name: 'xcode_version', value: xcode_version), 
                        string(name: 'branch_to_build', value: BRANCH), 
                        string(name: 'build_number_override', value: env.BUILD_NUMBER),
                    ]
                )

            }
        }
    }
}