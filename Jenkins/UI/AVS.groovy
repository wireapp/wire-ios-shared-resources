pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    parameters {
        string(defaultValue: "", description: 'Version of AVS to use', name: 'avs_version')
        string(defaultValue: "develop", description: 'Branch to use', name: 'branch_to_build')
    }

    stages {
        stage ("Trigger build") {
            steps {
                build(
                    job: 'client-ios-build-pipeline', 
                    parameters: [
                        string(name: 'developer_dir', value: '/Applications/Xcode.app/Contents/Developer'),
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
