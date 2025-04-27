
// timeout(300) {
//     node('python') {
        

//         currentBuild.description = """
//         BRANCH=${REFSPEC}
//         Owner=${env.BUILD_USER}
//         """
//                 // Owner=${env.BUILD_USER}

//         stage('Checkout') {
//             dir('api-tests') {
//                 checkout scm
//             }
//         }

//         stage('Debug') {
//             echo "Build causes: ${currentBuild.getBuildCauses()}"
//             echo "Build user: ${env.BUILD_USER}"
//         }

//         // stage('Runnung test') {
//         //     extCode = sh(
//         //         script: 'sudo docker run --rm --name=uitests -t localhost:5005/ui_tests:1.0.0',
//         //         returnStatus: true
//         //     )
//         // }


//         // stage('Publish allure report') {
//         //     dir('api-tests') {
//         //         allure({
//         //             includeProperties: false,
//         //             jdk: '',
//         //             properties: [],
//         //             reportBuildPolicy: 'ALWAYS',
//         //             results: [[path: './allure-results']]
//         //         })
//         //     }
//         // }

//         def configScriptPath = './config/config.py'

//         stage('Generate job.ini config') {
//             dir('api-tests') {
//                 withCredentials([usernamePassword(credentialsId: 'jobs_builder_creds', usernameVariable: 'username', passwordVariable: 'password')]) {
//                     sh "USER=${username} PASSWORD=${password} python3 ${configScriptPath}"
//                 }
                
//             }

//         }

//         stage('Start update jobs') {
//             dir('api-tests') {
//                 sh "jenkins-jobs --conf ./config/job.ini update ./jobs/"
//             }
//         }

//     }
// }

pipeline {
    agent any

    environment {
        // Автоматически определяем ветку
        BRANCH = "${env.BRANCH_NAME ?: sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()}"
        // Устанавливаем владельца
        Owner = "${currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause)?.userName ?: 'SYSTEM'}"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "Checking out the repository..."
                }
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [],
                    userRemoteConfigs: [[
                        url: 'git@github.com:NikolaiNM/Jenkins.git',
                        credentialsId: 'jenkins'
                    ]]
                ])
                echo "Branch: ${BRANCH}"
            }
        }

        stage('Debug') {
            steps {
                script {
                    echo "Build causes: ${currentBuild.rawBuild.getCauses()}"
                    echo "Resolved owner: ${Owner}"
                    echo "Branch: ${BRANCH}"
                }
            }
        }

        stage('Generate job.ini config') {
            steps {
                dir('api-tests') {
                    withCredentials([usernamePassword(credentialsId: 'your-credentials', usernameVariable: 'USER', passwordVariable: 'PASSWORD')]) {
                        sh '''
                            USER=${USER} PASSWORD=${PASSWORD} python3 ./config/config.py
                        '''
                    }
                }
            }
        }

        stage('Start update jobs') {
            steps {
                dir('api-tests') {
                    sh '''
                        jenkins-jobs --conf ./config/job.ini update ./jobs/
                    '''
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution completed.'
        }
    }
}