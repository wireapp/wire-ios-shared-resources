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
    options {
        ansiColor('xterm')
    }
    environment { 
        // This is where the misc configuration files (e.g. Gemfile) are fetched from. When testing swap out the branch
        DEPENDENCIES_BASE_URL = "https://raw.githubusercontent.com/wireapp/wire-ios-shared-resources/master"
        // For command line tools to be able to access API we set there env vars to values from Jenkins credentials store
        GITHUB_TOKEN = credentials('github-api-token')
        GITHUB_ACCESS_TOKEN = credentials('github-api-token')


        // Most fool-proof way to make sure rbenv and ruby works fine
        PATH = "/Users/ci/.rbenv/shims:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"

        // Turn off annoying update checks
        FASTLANE_SKIP_UPDATE_CHECK = "YES"

        // Repository from which to fetch custom AVS binary
	    AVS_REPO = "wireapp/avs-ios-binaries-appstore"

        CACHE_CARTHAGE = "${cache_carthage}"
    }
    parameters {        
        string(defaultValue: "Wire-iOS-languages", description: 'testplan', name: 'testplan')
        string(defaultValue: "12.4", description: 'XCode version to use (12.4)', name: 'xcode_version')
        string(defaultValue: "develop", description: 'Branch to use', name: 'branch_to_build')
        string(defaultValue: "", description: 'Version of AVS to use, only relevant for AVS build', name: 'avs_version')
        string(defaultValue: "999999", description: 'Override build number with', name: 'build_number_override')
        choice(
            choices: ["Development"], 
            description: 'Type of build', 
            name: "BUILD_TYPE"
        )
    }


    stages {
        stage('Checkout') {
            steps {

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
                                    echo "set DEVELOPER_DIR to XCode 12.4"
                                    export DEVELOPER_DIR=/Applications/Xcode_12.4.app/Contents/Developer
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
                    avs_version:${avs_version} \
                    xcode_version:${xcode_version} \
                    cache_carthage:${cache_carthage}
                """


                // Make sure that all subsequent steps see the branch from main project, not from build assets
                script {
                    GIT_BRANCH = "${branch_to_build}"
                }
            }
        }

        stage('Test') {
            steps {
                sh """#!/bin/bash -l
                    xcodebuild \
                      -project Wire-iOS.xcodeproj \
                      -scheme Wire-iOS \
                      -sdk iphonesimulator \
                      -destination 'platform=iOS Simulator,name=iPhone 8,OS=14.4' \
                      -resultBundlePath ./allLanguageTests.xcresult \
                      test \
                      -testPlan ${testplan} \
                      || true
                """
            }
        }

        stage('Convert XC result to JSON') {
            steps {
                sh """#!/bin/bash -l
                    xcrun xcresulttool get --path ./allLanguageTests.xcresult --format json > allLanguageTests.json
                """
            }
        }
        
        stage('Search for crashes') {
            steps {
                //case:  "_value" : "Test crashed with signal segv"
                sh """#!/bin/bash -l
                    grep -q '"_value" : "Test crashed' allLanguageTests.json && exit 1 || echo "no crash text is found"

                """

                //case:  "_value" : "Crash: Wire (12345): Namespace SIGNAL, Code 0xb"
                sh """#!/bin/bash -l
                    grep -q '"_value" : "Crash: Wire (' allLanguageTests.json && exit 1 || echo "no crash text is found"

                """
            }
        }
    }
}
