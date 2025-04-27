
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

// Добавляем явный импорт класса UserIdCause
import hudson.model.Cause.UserIdCause

timeout(300) {
    node('python') {
        // Определяем ветку через env.BRANCH_NAME
        def branch = env.BRANCH_NAME ?: 'unknown'

        // Определяем владельца через анализ причин сборки
        def owner = 'SYSTEM'
        def causes = currentBuild.getBuildCauses()
        
        causes.each { cause ->
            // Используем instanceof с явно импортированным классом
            if (cause instanceof UserIdCause) {
                owner = cause.userId ?: cause.userName
            }
        }

        currentBuild.description = """
        BRANCH=${branch}
        Owner=${owner}
        """.stripIndent()

        stage('Checkout') {
            dir('api-tests') {
                checkout scm
            }
        }

        stage('Debug') {
            echo "Build causes: ${causes}"
            echo "Resolved owner: ${owner}"
            echo "Branch: ${branch}"
        }

        // Остальные этапы остаются без изменений
        def configScriptPath = './config/config.py'

        stage('Generate job.ini config') {
            dir('api-tests') {
                withCredentials([usernamePassword(
                    credentialsId: 'jobs_builder_creds',
                    usernameVariable: 'USERNAME',
                    passwordVariable: 'PASSWORD'
                )]) {
                    sh "USER='$USERNAME' PASSWORD='$PASSWORD' python3 ${configScriptPath}"
                }
            }
        }

        stage('Start update jobs') {
            dir('api-tests') {
                sh "jenkins-jobs --conf ./config/job.ini update ./jobs/"
            }
        }
    }
}