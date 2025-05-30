pipeline {
    agent any

    environment {
        SERVICE_NAME = "user-service"
        EC2_USER = "ubuntu"
        EC2_HOST = "10.0.2.225"
        REMOTE_PATH = "/home/ubuntu/backend/${SERVICE_NAME}"
        SLACK_WEBHOOK = credentials('slack-webhook')
        BUILD_URL = "${env.BUILD_URL}"
        APP_PORT = "8080"
    }

    stages {
        stage('Start Application') {
            steps {
                sshagent(['PRIVATE_EC2_KEY']) {
                    script {
                        def output = sh(
                                script: """
                                ssh -o StrictHostKeyChecking=no $EC2_USER@$EC2_HOST "\
                                  chmod +x $REMOTE_PATH/start.sh && \
                                  $REMOTE_PATH/start.sh"
                            """,
                                returnStdout: true
                        ).trim()

                        echo "🔧 start.sh 실행 로그:"
                        echo output

                        def lines = output.readLines()
                        env.START_LOG_TAIL = lines.takeRight(20).join("\\n")

                        def resultLine = lines.find { it.contains('[RESULT]') } ?: '[RESULT] UNKNOWN'
                        env.START_RESULT = resultLine
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                def result = env.START_RESULT ?: '[RESULT] UNKNOWN'
                def log = env.START_LOG_TAIL ?: '(start.sh 로그 없음)'

                def statusMessage = ""
                if (result.contains("SUCCESS")) {
                    statusMessage = ":rocket: *[${SERVICE_NAME}]* 배포 성공!"
                } else if (result.contains("ROLLBACK_SUCCESS")) {
                    statusMessage = ":warning: *[${SERVICE_NAME}]* 배포 실패 → 롤백 성공!"
                } else if (result.contains("ROLLBACK_FAILED")) {
                    statusMessage = ":fire: *[${SERVICE_NAME}]* 배포 및 롤백 모두 실패!"
                } else {
                    statusMessage = ":grey_question: *[${SERVICE_NAME}]* 배포 상태 미확인!"
                }

                def message = """
${statusMessage}
➡️ <${BUILD_URL}|Jenkins 로그 보기>
📄 *start.sh 로그 (최근 20줄)*:
\\`\\`\\`
${log}
\\`\\`\\`
"""

                sh """
                curl -X POST -H 'Content-type: application/json' --data '{"text": "${message}"}' ${SLACK_WEBHOOK}
                """
            }
        }
    }
}
