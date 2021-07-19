pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    parameters {
        string(defaultValue: "", description: 'Version of AVS to use', name: 'avs_version')
        string(defaultValue: "develop", description: 'Branch to use', name: 'branch_to_build')
        string(defaultValue: "12.4", description: 'XCode version to use (12.4)', name: 'xcode_version')
    }

    stages {
        stage ("Trigger build") {
            steps {
                build(
                    job: 'client-ios-build-pipeline', 
                    parameters: [
                        string(name: 'xcode_version', value: xcode_version), 
                        string(name: 'branch_to_build', value: branch_to_build), 
                        string(name: 'BUILD_TYPE', value: 'AVS'), 
                        string(name: 'avs_version', value: avs_version),
                        string(name: 'build_number_override', value: env.BUILD_NUMBER)
                    ]
                )
            }
        }
    }
}
