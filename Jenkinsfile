pipeline {
    agent any

    environment {
        SERVICE_NAME = "user-service"
        EC2_USER = "ubuntu"
        EC2_HOST = "10.0.2.225"
        REMOTE_PATH = "/home/ubuntu/backend/${SERVICE_NAME}"
        BUILD_URL = "${env.BUILD_URL}"
    }

    stages {
        stage('Git Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/GoormTeam1/user-service'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew clean test'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean bootJar'
            }
        }

        stage('Deploy') {
            steps {
                sshagent(['PRIVATE_EC2_KEY']) {
                    script {
                        def output = sh(
                                script: """
                                ssh -o StrictHostKeyChecking=no $EC2_USER@$EC2_HOST 'mkdir -p $REMOTE_PATH'
                                scp -o StrictHostKeyChecking=no build/libs/*.jar $EC2_USER@$EC2_HOST:$REMOTE_PATH/user.jar
                                scp -o StrictHostKeyChecking=no start.sh $EC2_USER@$EC2_HOST:$REMOTE_PATH/start.sh
                                ssh -o StrictHostKeyChecking=no $EC2_USER@$EC2_HOST "chmod +x $REMOTE_PATH/start.sh && $REMOTE_PATH/start.sh"
                            """,
                                returnStdout: true
                        ).trim()

                        echo "🔧 start.sh 실행 로그:"
                        echo output

                        def lines = output.readLines()
                        env.START_LOG_TAIL = lines.takeRight(20).join("\\n")
                        env.START_RESULT = lines.find { it.contains('[RESULT]') } ?: '[RESULT] UNKNOWN'
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

                def rawMessage = """
${statusMessage}
➡️ <${BUILD_URL}|Jenkins 로그 보기>

📄 *start.sh 로그 (최근 20줄)*:
\\`\\`\\`
${log}
\\`\\`\\`
"""

                def safeMessage = rawMessage
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")

                withCredentials([string(credentialsId: 'slack-webhook', variable: 'WEBHOOK_URL')]) {
                    sh """
                    cat <<EOF | curl -X POST -H 'Content-type: application/json' -d @- "\$WEBHOOK_URL"
                    {
                      "text": "${safeMessage}"
                    }
EOF
                    """
                }
            }
        }
    }
}
