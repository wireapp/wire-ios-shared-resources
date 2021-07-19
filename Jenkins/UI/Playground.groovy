pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    parameters {
        string(defaultValue: "develop", description: 'Branch to use', name: 'branch_to_build')
        string(defaultValue: "12.4", description: 'XCode version to use (11.4.1/12.4)', name: 'xcode_version')
        string(defaultValue: "No", description: 'Copy Carthage folder from cache? Enter Yes to enable (NOTICE: it builds faster, but may cause build issues)', name: 'cache_carthage')
        string(defaultValue: "No", description: 'skip tests to speed up pipeline', name: 'skip_test')
    }

    stages {
        stage ("Trigger build") {
            steps {
                build(
                    job: 'client-ios-build-pipeline', 
                    parameters: [
                        string(name: 'skip_test', value: skip_test), 
                        string(name: 'cache_carthage', value: cache_carthage), 
                        string(name: 'xcode_version', value: xcode_version), 
                        string(name: 'branch_to_build', value: branch_to_build), 
                        string(name: 'BUILD_TYPE', value: 'Playground'), 
                        string(name: 'build_number_override', value: env.BUILD_NUMBER)
                    ]
                )
            }
        }
    }
}

