pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    
    parameters {
        string(defaultValue: "/Applications/Xcode.app/Contents/Developer", description: 'XCode to use', name: 'developer_dir')
    }

    stages {
        stage ("Trigger build") {
            steps {
                build(
                    job: 'client-ios-build-pipeline', 
                    parameters: [
                        string(name: 'developer_dir', value: developer_dir), 
                        string(name: 'branch_to_build', value: branch_to_build), 
                        string(name: 'BUILD_TYPE', value: 'Playground'), 
                        string(name: 'build_number_override', value: env.BUILD_NUMBER)
                    ]
                )
            }
        }
    }
}

