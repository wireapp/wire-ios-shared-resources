pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    parameters {
        string(defaultValue: "develop", description: 'Branch to use', name: 'branch_to_build')
    }

    stages {
        stage ("Trigger build") {
            steps {
                build(
                    job: 'client-ios-build-pipeline', 
                    parameters: [
                        string(name: 'branch_to_build', value: branch_to_build), 
                        string(name: 'BUILD_TYPE', value: 'Playground'), 
                        string(name: 'build_number_override', value: env.BUILD_NUMBER)
                    ]
                )
            }
        }
    }
}

