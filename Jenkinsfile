pipeline {
    agent any

    environment {
        SERVICE_NAME = "user-service"
        EC2_USER = "root"
        EC2_HOST = "10.0.2.225"
        REMOTE_PATH = "/home/ubuntu/backend/$SERVICE_NAME"
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
                    sh """
                    ssh -o StrictHostKeyChecking=no $EC2_USER@$EC2_HOST 'mkdir -p $REMOTE_PATH'
                    scp -o StrictHostKeyChecking=no build/libs/*.jar $EC2_USER@$EC2_HOST:$REMOTE_PATH/user.jar
                    scp -o StrictHostKeyChecking=no start.sh $EC2_USER@$EC2_HOST:$REMOTE_PATH/start.sh
                    ssh -o StrictHostKeyChecking=no $EC2_USER@$EC2_HOST "chmod +x $REMOTE_PATH/start.sh && $REMOTE_PATH/start.sh"
                    """

                }
            }
        }
    }
}
