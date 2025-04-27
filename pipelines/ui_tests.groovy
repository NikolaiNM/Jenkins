import groovy.json.JsonSlurperClassic

timeout(300) {
    node('maven') {
        def userIdCause = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')?.find { it }
        def owner = userIdCause?.userName ?: 'admin'

        currentBuild.description = """
        BRANCH=${REFSPEC}
        Owner=${owner}
        """
        stage('Checkout') {
            dir('api-tests') {
                checkout scm
            }
        }

        def yamlConfig = readYaml text: $YAML_CONFIG

        stage('Running tests') {
            def exitCode = script
        }

        stage('Runnung tests') {
            extCode = sh(
                script: "docker run --rm --name=uitests -t localhost:5005/ui_tests:1.0.0"
                script: "docker run --network=host ..."
                //script: "mvn test -Dbrowser=${yamlConfig['BROWSER']} -DbrowserVersion=${yamlConfig['BROWSER_VERSION']}",
                returnStatus: true
            )

            if(exitCode > 0) {
                currentBuild.status = 'UNSTABLE'
            }
        }

        // stage('Publish allure report') {
        //     dir('api-tests') {
        //         allure({
        //             includeProperties: false,
        //             jdk: '',
        //             properties: [],
        //             reportBuildPolicy: 'ALWAYS',
        //             results: [[path: './allure-results']]
        //         })
        //     }
        // }

    //     stage('Send notification') {
    //         def report = readFile './allure-report/widgets/summary.json'
    //         def slurped = new JsonSlurperClassic().parseText(report)

    //         getNotifyMessage(slurped)
    //     }

    // }

    // def getNotifyMessage(statistic) {
    //     def message = "============ Report =============\n"
    //     statistic.each {k, v ->
    //         message += "\t${k}: ${v\n}"        
    //     }
    
    //     withCredentials(string([credentialsId: chat_id, var: chat_id]), string ([credentialsId: token, var: botToken])) {
    //       sh "curl -s -X POST -H 'Content-Type: application/json' -d '{\"chat_id\": \"${chat_id}\", \"text\": \"${message}\"}' https://api.telegram.org/bot${botToken}/sendMessage"     
    //     }
    // }

}
}