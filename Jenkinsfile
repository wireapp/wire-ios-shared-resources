pipeline {
    agent any
    environment { 
        DEPENDENCIES_BASE_URL = "https://raw.githubusercontent.com/wireapp/wire-ios-shared-resources/feature/fastlane"
        GITHUB_TOKEN = credentials('github-api-token')
        GITHUB_ACCESS_TOKEN = credentials('github-api-token')
    }
    parameters {
        choice(
            choices: ['wire-ios-canvas', 'wire-ios-cryptobox', 'wire-ios-data-model', 'wire-ios-images', 'wire-ios-link-preview', 'wire-ios-mocktransport', 'wire-ios-protos', 'wire-ios-request-strategy', 'wire-ios-scripting-helpers', 'wire-ios-share-engine', 'wire-ios-sync-engine', 'wire-ios-system', 'wire-ios-testing', 'wire-ios-transport', 'wire-ios-utilities', 'wire-ios-ziphy'], 
            description: 'The framework for which you want to release a new version.', 
            name: 'BOT_FRAMEWORK'
        )
        choice(
            choices: ['patch', 'minor', 'major'], 
            description: 'Type of release, as defined in: http://semver.org/', 
            name: 'BOT_TYPE'
        )
        booleanParam(
            defaultValue: true, 
            description: 'Whether to run tests. If unchecked, will skip tests.', 
            name: 'RUN_TESTS'
        )
    }
    stages {
        stage('Prepare') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/develop']],
                    extensions: [[$class: 'LocalBranch']],
                    userRemoteConfigs: [[url: "git@github.com:wireapp/${BOT_FRAMEWORK}.git"], [credentialsId:'wire-bot-ssh-key']]
                ])
                
                sh "curl -O ${DEPENDENCIES_BASE_URL}/Gemfile"
                sh "curl -O ${DEPENDENCIES_BASE_URL}/Gemfile.lock"
            }
        }
        stage('Build') {
            when {
                expression { params.RUN_TESTS == true }
            }
            steps {
                sh '''
                    eval "$(rbenv init -)"
                    bundle install --path ~/.gem
                    bundle exec fastlane build
                '''
            }
        }
        
        stage('Test') {
            when {
                expression { params.RUN_TESTS == true }
            }
            steps {
                sh '''
                    eval "$(rbenv init -)"
                    bundle exec fastlane test
                '''
            }
        }
        stage('Release') {
            steps {
                sh """
                    eval "\$(rbenv init -)"
                    bundle install --path ~/.gem
                    bundle exec fastlane release type:${BOT_TYPE}
                """
            }
        }
    }
    // post {
    //     always {
    //         cleanWs()
    //     }
    // }
}


