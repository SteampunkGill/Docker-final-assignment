pipeline {
    agent none  // 不使用全局 agent

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
    }

    stages {
        stage('1. Run Unit Tests') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    args '-v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                dir('backend/backend') {
                    sh 'mvn test'
                }
            }
        }

        stage('2. Build & Package') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    args '-v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                dir('backend/backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('3. Build & Push Docker Image') {
            agent any
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'DOCKERHUB_CREDENTIALS') {
                        dir('backend') {
                            def app = docker.build("${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}")
                            app.push()
                            app.push('latest')
                        }
                    }
                }
            }
        }

        stage('4. Cleanup') {
            agent any
            steps {
                sh """
                    docker rmi ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} || true
                    docker rmi ${BACKEND_IMAGE_NAME}:latest || true
                """
            }
        }
    }

    post {
        always {
            echo '收集测试报告...'
            junit 'backend/target/surefire-reports/*.xml'
            node('') {
                sh 'docker builder prune -f || true'
            }
        }
        success {
            echo "✅ 镜像已推送: ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}"
        }
        failure {
            echo '❌ Pipeline 执行失败'
        }
    }
}
