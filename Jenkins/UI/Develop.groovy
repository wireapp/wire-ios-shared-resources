pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    triggers { 
        pollSCM('H/4 * * * *') 
    }
    environment {
        BRANCH = "develop"
    }
    stages {
        stage('Checkout') {
            steps {
                // Checking out the correct repository. This is dynamic because we have one job that can build multiple frameworks
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: BRANCH]], // Checks out specified branch
                    extensions: [
                        [$class: 'AuthorInChangelog'],
                        [$class: 'CloneOption', noTags: true, reference: '', shallow: true],
                        [$class: 'CheckoutOption', timeout: 30],
                        [$class: 'LocalBranch', localBranch: '**'], // Unless this is specified, it simply checks out by commit SHA with no branch information
                        [$class: 'CleanBeforeCheckout'] // Resets untracked files, just to make sure we are clean
                    ],
                    userRemoteConfigs: [[url: "git@github.com:wireapp/wire-ios.git"]]
                ])
            }
        }
        stage ("Trigger build") {
            steps {
                build(
                    job: 'client-ios-build-pipeline', 
                    parameters: [
                        string(name: 'branch_to_build', value: BRANCH), 
                        string(name: 'BUILD_TYPE', value: 'Development'), 
                        string(name: 'build_number_override', value: env.BUILD_NUMBER)
                    ]
                )
            }
        }
    }
}

