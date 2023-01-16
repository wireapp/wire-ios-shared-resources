pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    triggers { 
        pollSCM('H 21 * * *') 
    }
    environment {
        BRANCH = "develop"
        LAST_COMMIT = ""
    }
    parameters {
        choice(
            choices: ["13.2.1"],
            description: 'Xcode version to build with.',
            name: "xcode_version"
        )
    }
    stages {
        stage('Checkout') {
            steps {
                script {
                    def scmVars = checkout([
                        $class: 'GitSCM',
                        branches: [[name: BRANCH]], // Checks out specified branch
                        extensions: [
                            [$class: 'AuthorInChangelog'],
                            [$class: 'CloneOption', noTags: true, reference: '', shallow: true],
                            [$class: 'CheckoutOption', timeout: 30],
                            [$class: 'LocalBranch', localBranch: '**'], // Unless this is specified, it simply checks out by commit SHA with no branch information
                            [$class: 'CleanBeforeCheckout'] // Resets untracked files, just to make sure we are clean
                        ],
                        userRemoteConfigs: [[url: "git@github.com:wireapp/wire-ios-mono.git"]]
                    ])

                    LAST_COMMIT = scmVars.GIT_PREVIOUS_COMMIT ?: ""
                }
            }
        }
        stage ("Trigger build") {
            steps {
                build(
                    job: 'client-ios-mono-build-pipeline', 
                    parameters: [
                        string(name: 'xcode_version', value: xcode_version), 
                        string(name: 'branch_to_build', value: BRANCH), 
                        string(name: 'BUILD_TYPE', value: 'Internal'), 
                        string(name: 'build_number_override', value: env.BUILD_NUMBER),
                        string(name: 'last_commit_for_changelog', value: LAST_COMMIT)
                    ]
                )
            }
        }
    }
}

