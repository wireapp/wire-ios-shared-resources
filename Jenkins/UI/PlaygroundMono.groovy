pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    parameters {
        string(
            defaultValue: "develop", 
            description: 'Branch to build from.', 
            name: 'branch_to_build'
        )
        choice(
            choices: ["13.2.1"],
            description: 'Xcode version to build with.',
            name: "xcode_version"
        )
    }

    stages {
        stage ("Trigger build") {
            steps {
                build(
                    job: 'wire-ios-mono-build', 
                    parameters: [
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