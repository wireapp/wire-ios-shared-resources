pipeline {
    agent { 
        // This job will be run on all machines that have this label set up
        label 'frameworks'
    }
    environment { 
        // This is where the misc configuration files (e.g. Gemfile) are fetched from. When testing swap out the branch
        DEPENDENCIES_BASE_URL = "https://raw.githubusercontent.com/wireapp/wire-ios-shared-resources/master"
        // For command line tools to be able to access API we set there env vars to values from Jenkins credentials store
        GITHUB_TOKEN = credentials('github-api-token')
        GITHUB_ACCESS_TOKEN = credentials('github-api-token')
        // Most fool-proof way to make sure rbenv and ruby works fine
        PATH = "/Users/ci/.rbenv/shims:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
    }
    
    stages {
        // Will merge the PR that triggered this job using GitHub API
        stage('Merge PR') {
            steps {
                // Delete the workspace
                cleanWs()
                // We are not checking out the repository, so need to manually download Gemfile AND Fastlane config for the job
                sh """ #!/bin/bash -l
                    curl -O ${DEPENDENCIES_BASE_URL}/Gemfile
                    curl -O ${DEPENDENCIES_BASE_URL}/Gemfile.lock
                    mkdir fastlane
                    curl -o fastlane/Fastfile ${DEPENDENCIES_BASE_URL}/Fastlane/MergePR
                    bundle install --path ~/.gem

                    bundle exec fastlane fastlane merge_pr number:${ghprbPullId} repo:${ghprbGhRepository}
                """
            }
        }
        // Triggers the other job that actually makes a release with type taken from the trigger phrase
        stage('Trigger build') {
            steps {
                echo "Pull ID=$ghprbPullId, comment:$ghprbCommentBody, repo:$ghprbGhRepository"
                build job: 'new-framework-release', wait: false, parameters: [
                    [$class: 'StringParameterValue', name: 'BOT_FRAMEWORK', value: framework_name("$ghprbGhRepository")],
                    [$class: 'StringParameterValue', name: 'BOT_TYPE', value: release_type("$ghprbCommentBody")],
                    [$class: 'BooleanParameterValue', name: 'RUN_TESTS', value: true]
                ]
            }
        }
    }
}

// Extracts the release type from comment ("@zenkins release patch" -> "patch"). Needs to be @NonCPS because it uses API that is now allowed in declarative pipelines
@NonCPS
def release_type(comment) {
    def match = comment =~ /\@zenkins release (patch|minor|major)/

    if (match.find()) {
        return match[0][1]
    }
    return null
}

// Extracts framework name from repository name ("wireapp/wire-ios-canvas" -> "wire-ios-canvas")
@NonCPS
def framework_name(repo) {
    return repo.tokenize('/')[1]
}
