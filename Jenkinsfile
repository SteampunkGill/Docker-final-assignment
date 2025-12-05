pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
        DOCKER_COMPOSE_PATH = "${env.WORKSPACE}/bin"
        // ✅ 定义 compose 命令，方便复用
        COMPOSE_CMD = "docker-compose -f docker-compose.yml -f docker-compose.ci.yml"
    }

    stages {
        stage('1. Setup Environment') {
            steps {
                script {
                    echo "Checking and preparing docker-compose for Linux..."
                    sh '''
                        if [ ! -f "${DOCKER_COMPOSE_PATH}/docker-compose" ]; then
                            echo "docker-compose not found, downloading..."
                            mkdir -p "${DOCKER_COMPOSE_PATH}"
                            curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o "${DOCKER_COMPOSE_PATH}/docker-compose"
                            chmod +x "${DOCKER_COMPOSE_PATH}/docker-compose"
                        else
                            echo "docker-compose already exists."
                        fi
                    '''
                    echo "Verifying docker-compose version..."
                    sh "'${DOCKER_COMPOSE_PATH}/docker-compose' --version"
                }
            }
        }

        stage('Build, Test & Push') {
            environment {
                PATH = "${DOCKER_COMPOSE_PATH}:${env.PATH}"
            }
            stages {
                stage('2. Checkout Code') {
                    steps {
                        checkout scm
                    }
                }

                stage('3. Run Unit Tests') {
                    agent {
                        docker {
                            image 'maven:3.9-eclipse-temurin-17'
                            reuseNode true
                        }
                    }
                    steps {
                        dir('backend/backend') {
                            sh 'mvn test'
                        }
                    }
                }

                stage('4. Build & Package') {
                    agent {
                        docker {
                            image 'maven:3.9-eclipse-temurin-17'
                            reuseNode true
                        }
                    }
                    steps {
                        dir('backend/backend') {
                            sh 'mvn package -DskipTests'
                        }
                    }
                }

                // =====================================================================
                // STAGE 5: 增加了打印文件内容的最终调试步骤
                // =====================================================================
                stage('5. Integration Tests') {
                    steps {
                        script {
                            try {
                                echo 'Ensuring a clean environment...'
                                // ✅ 使用新的 compose 命令
                                sh "${COMPOSE_CMD} down --remove-orphans"

                                // ✅ 最终调试步骤: 打印两个 compose 文件的内容以供检查
                                echo '------------------------------------------------------------'
                                echo '---               Content of docker-compose.yml          ---'
                                echo '------------------------------------------------------------'
                                sh 'cat docker-compose.yml'

                                echo '------------------------------------------------------------'
                                echo '---             Content of docker-compose.ci.yml         ---'
                                echo '------------------------------------------------------------'
                                sh 'cat docker-compose.ci.yml'
                                echo '------------------------------------------------------------'


                                echo 'Starting the application environment for integration tests...'
                                // ✅ 使用新的 compose 命令
                                sh "${COMPOSE_CMD} up -d"

                                echo 'Waiting for services to start (20 seconds)...'
                                sleep(20)

                                // ✅ 修复端口为 8081
                                echo 'Performing health check on port 8081...'
                                sh 'curl -f http://localhost:8081/actuator/health'

                            } finally {
                                echo 'Integration tests finished. Tearing down the environment...'
                                // ✅ 清理时也必须使用同一个 compose 命令
                                sh "${COMPOSE_CMD} down"
                            }
                        }
                    }
                }

                stage('6. Build & Push Docker Image') {
                    steps {
                        withCredentials([usernamePassword(
                            credentialsId: 'DOCKERHUB_CREDENTIALS',
                            passwordVariable: 'DOCKERHUB_PASSWORD',
                            usernameVariable: 'DOCKERHUB_USERNAME'
                        )]) {
                            sh "docker login -u ${DOCKERHUB_USERNAME} -p ${DOCKERHUB_PASSWORD}"
                            sh "docker build -t ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} -f backend/Dockerfile backend"
                            sh "docker tag ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} ${BACKEND_IMAGE_NAME}:latest"
                            sh "docker push ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}"
                            sh "docker push ${BACKEND_IMAGE_NAME}:latest"
                        }
                    }
                }

                stage('7. Cleanup') {
                    steps {
                        sh 'docker logout'
                    }
                }
            }
        }
    }

    post {
        always {
            echo '收集测试报告...'
            junit allowEmptyResults: true, testResults: 'backend/backend/target/surefire-reports/*.xml'
        }
    }
}
