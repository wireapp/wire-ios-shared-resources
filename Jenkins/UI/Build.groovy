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

        // Repository from which to fetch custom AVS binary
	    AVS_REPO = "wireapp/avs-ios-binaries-appstore"

        XCODE_VERSION = "${xcode_version}"
        CACHE_CARTHAGE = "${cache_carthage}"
    }
    parameters {
        string(defaultValue: "develop", description: 'Branch to use', name: 'branch_to_build')
        choice(
            choices: ["Playground", "Development", "Internal", "AVS", "RC"], 
            description: 'Type of build', 
            name: "BUILD_TYPE"
        )
        string(defaultValue: "", description: 'Version of AVS to use, only relevant for AVS build', name: 'avs_version')
        string(defaultValue: "", description: 'Override build number with', name: 'build_number_override')
        string(defaultValue: "", description: 'Produces changelog from all commits added since this commit', name: 'last_commit_for_changelog')
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
                                    echo "set DEVELOPER_DIR to XCode 13.1"
                                    export DEVELOPER_DIR=/Applications/Xcode_13.1.app/Contents/Developer
		                """
	            	}
	            }
	        }
	    }


        stage('fastlane prepare') {
            steps {
                sh """#!/bin/bash -l
                    bundle exec fastlane prepare build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE} avs_version:${avs_version} xcode_version:${xcode_version} cache_carthage:${cache_carthage}
                """


                // Make sure that all subsequent steps see the branch from main project, not from build assets
                script {
                    GIT_BRANCH = "${branch_to_build}"
                }
            }
        }

        stage('Build') {
            steps {
                sh """#!/bin/bash -l
                    bundle exec fastlane build \
                     build_number:${BUILD_NUMBER} \
                     build_type:${BUILD_TYPE} \
                     configuration:Debug \
                     for_simulator:true \
                     xcode_version:${xcode_version}
                """
            }
        }

        stage('Test') {
            steps {
                sh """#!/bin/bash -l
                    bundle exec fastlane test xcode_version:${xcode_version}
                """
            }
        }
        
        stage("QA: build for simulator") {
            steps {
                sh """#!/bin/bash -l
                    bundle exec fastlane build_for_release build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE} configuration:Debug for_simulator:true xcode_version:${xcode_version}
                """
            }
        }

        stage("QA: build for device") {
            steps {
                sh """#!/bin/bash -l
                    bundle exec fastlane build_for_release build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE} configuration:Debug for_simulator:false xcode_version:${xcode_version}
                """
            }
        }
        stage('Build for release') {
            steps {
                sh """#!/bin/bash -l
                    bundle exec fastlane build_for_release build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE} xcode_version:${xcode_version}
                """
            }
        }
        stage("Upload results & code converage") {
            parallel {
                stage("Upload to S3") {
                    steps {
                        sh """#!/bin/bash -l
                            bundle exec fastlane upload_s3 build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE}
                        """
                    }
                }

                stage('Upload to AppCenter') {
                    steps {
                        sh """#!/bin/bash -l
                            bundle exec fastlane upload_app_center build_number:${BUILD_NUMBER} build_type:${BUILD_TYPE} last_commit:${last_commit_for_changelog} avs_version:${avs_version}
                        """
                    }
                }

//                 stage('Slather & Cobertura') {
//                     steps {
//                         sh """#!/bin/bash -l
//                             slather
//                         """
//                         cobertura autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: '**/test-reports/cobertura.xml', conditionalCoverageTargets: '70, 0, 0', failUnhealthy: false, failUnstable: false, lineCoverageTargets: '80, 0, 0', maxNumberOfBuilds: 0, methodCoverageTargets: '80, 0, 0', onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: false
//                     }
//                 }
            }
        }
    }
    post {
        always {

            junit testResults: "test/*.junit", allowEmptyResults: true
        }
        success {
            sh """
            curl -s https://gist.githubusercontent.com/marcoconti83/781c13a1c3faa55e20595015d929e2ca/raw/upload_junit.py | python - \
                --job_name ${parentBuild["name"]} \
                --build_number ${parentBuild["number"]} \
                "ios" \
                "test/report.junit" \
                || echo "FAILED TO UPLOAD TESTS"
            """
        }
    }
}
