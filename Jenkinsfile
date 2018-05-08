pipeline {
    agent any
    parameters {
        choice(
            choices: ['wire-ios-canvas', 'wire-ios-cryptobox', 'wire-ios-data-model', 'wire-ios-images', 'wire-ios-link-preview', 'wire-ios-mocktransport', 'wire-ios-protos', 'wire-ios-request-strategy', 'wire-ios-scripting-helpers', 'wire-ios-share-engine', 'wire-ios-sync-engine', 'wire-ios-system', 'wire-ios-testing', 'wire-ios-transport', 'wire-ios-utilities', 'wire-ios-ziphy'], 
            description: 'The framework for which you want to release a new version.', 
            name: 'BOT_FRAMEWORK')
        ),
        choice(
            choices: ['patch', 'minor', 'major'], 
            description: 'Type of release, as defined in: http://semver.org/', 
            name: 'BOT_TYPE'
        ),
        booleanParam(
            defaultValue: true, 
            description: 'Whether to run tests. If unchecked, will skip tests.', 
            name: 'RUN_TESTS'
        )
    }
    stages {
        stage('Build') {
            steps {
                echo 'Building ${params.BOT_FRAMEWORK}...'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing: ${params.RUN_TESTS}'
            }
        }
        stage('Release') {
            steps {
                echo 'Deploying ${params.BOT_TYPE}'
            }
        }
    }
}


