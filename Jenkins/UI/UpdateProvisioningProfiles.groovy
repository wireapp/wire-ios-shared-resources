@NonCPS
def getParentBuild() {
    def upstream = currentBuild.rawBuild.getCause(hudson.model.Cause$UpstreamCause)
    def upstreamJob = upstream?.upstreamProject ?: env.JOB_NAME
    def upstreamNumber = upstream?.upstreamBuild ?: env.BUILD_NUMBER
    return ["name":upstreamJob, "number":upstreamNumber]
}
def parentBuild = getParentBuild()

pipeline {
    agent any
    triggers { 
        pollSCM('H/4 * * * *') 
    }
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
    
        // For provisioning profiles update
        APPSTORE_CONNECT_TEAM_ID = credentials('appstore_connect_team_id')

        // Most fool-proof way to make sure rbenv and ruby works fine
        PATH = "/Users/ci/.rbenv/shims:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"

        // Turn off annoying update checks
        FASTLANE_SKIP_UPDATE_CHECK = "YES"

        // This will be set to app build number
        BUILD_NUMBER = "${env.BUILD_NUMBER}"

        // Repository from which to fetch custom AVS binary
	    AVS_REPO = "wireapp/avs-ios-binaries-appstore"

        // DATADOG API
        DATADOG_API_KEY = credentials('datadog_api_key')

        XCODE_VERSION = "${xcode_version}"
        CACHE_CARTHAGE = "${cache_carthage}"
    }
	
    parameters {
        choice(
            choices: ["Playground", "Development", "Internal", "AVS", "RC"], 
            description: 'Type of build', 
            name: "BUILD_TYPE"
        )
        choice(
            choices: ["Release", "Debug"], 
            description: 'Configuration of build', 
            name: "BUILD_CONFIGURATION"
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
		                        branches: [[name: '*/feat/FS-1436-port-apps']], // Checks out specified branch
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
                    branch: "master"
                } 
            }
            steps {
                withEnv([
                    "FASTLANE_USER=${APPSTORE_CONNECT_USER}",
                    "FASTLANE_PASSWORD=${APPSTORE_CONNECT_PASSWORD}",
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
