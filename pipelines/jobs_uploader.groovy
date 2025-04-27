
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

timeout(300) {
    node('python') {
        // Определяем владельца через анализ причин сборки
        def owner = 'SYSTEM'
        def buildCause = currentBuild.getBuildCauses()[0]
        if (buildCause.getClass().getName() == 'hudson.model.Cause$UserIdCause') {
            owner = buildCause.userId
        }

        currentBuild.description = """
        BRANCH=${env.GIT_BRANCH}
        Owner=${owner}
        """.stripIndent()

        stage('Checkout') {
            dir('api-tests') {
                checkout scm
            }
        }

        stage('Debug') {
            echo "Build causes: ${currentBuild.getBuildCauses()}"
            echo "Resolved owner: ${owner}"
        }

        def configScriptPath = './config/config.py'  // Путь относительно директории api-tests

        stage('Generate job.ini config') {
            dir('api-tests') {
                withCredentials([usernamePassword(
                    credentialsId: 'jobs_builder_creds',
                    usernameVariable: 'USERNAME',
                    passwordVariable: 'PASSWORD'
                )]) {
                    // Используем двойные кавычки для интерполяции переменных
                    sh "USER='$USERNAME' PASSWORD='$PASSWORD' python3 ${configScriptPath}"
                }
            }
        }

        stage('Start update jobs') {
            dir('api-tests') {
                // Проверяем существование конфига
                sh 'ls -la ./config/job.ini || echo "Config file not found!"'
                sh "jenkins-jobs --conf ./config/job.ini update ./jobs/"
            }
        }
    }
}