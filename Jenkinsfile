pipeline {
    agent any

    environment {
        SERVICE_NAME = "user-service"
        EC2_USER = "ubuntu"
        EC2_HOST = "10.0.2.225"
        REMOTE_PATH = "/home/ubuntu/backend/$SERVICE_NAME"
        SLACK_WEBHOOK = credentials('slack-webhook')
        BUILD_URL = "${env.BUILD_URL}"
        APP_PORT = "8080"
    }

    stages {
        stage('Start Application') {
            steps {
                sshagent(['PRIVATE_EC2_KEY']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no $EC2_USER@$EC2_HOST "\
                      chmod +x $REMOTE_PATH/start.sh && \
                      $REMOTE_PATH/start.sh"
                    """
                }
            }
        }

        stage('Wait for Server') {
            steps {
                echo "🕒 애플리케이션 시작 대기 중 (10초)..."
                sleep(time: 10, unit: 'SECONDS')
            }
        }

        stage('Health Check') {
            steps {
                echo "🔍 애플리케이션 헬스 체크 중..."
                script {
                    def result = sh(
                            script: "curl -sf http://$EC2_HOST:$APP_PORT/actuator/health",
                            returnStatus: true
                    )

                    if (result != 0) {
                        error("❌ 헬스 체크 실패! 앱이 정상 실행되지 않았습니다.")
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                def message = """
:rocket: *[${SERVICE_NAME}]* 배포 성공!
✅ start.sh 실행 및 헬스 체크 통과
➡️ <${BUILD_URL}|배포 로그 보기>
"""
                sh """
                curl -X POST -H 'Content-type: application/json' --data '{"text": "${message}"}' ${SLACK_WEBHOOK}
                """
            }
        }

        failure {
            script {
                def message = """
:fire: *[${SERVICE_NAME}]* 배포 실패!
❌ 헬스 체크 또는 배포 중 오류 발생
➡️ <${BUILD_URL}|자세한 로그 보기>
"""
                sh """
                curl -X POST -H 'Content-type: application/json' --data '{"text": "${message}"}' ${SLACK_WEBHOOK}
                """
            }
        }
    }
}
