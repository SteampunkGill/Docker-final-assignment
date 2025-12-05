pipeline {
    agent any

    environment {
        DOCKOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
        DOCKER_COMPOSE_PATH = "${env.WORKSPACE}/bin"
        // 我们不再需要任何覆盖文件或复杂的命令
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
                            args "-v ${env.WORKSPACE}/settings.xml:/root/.m2/settings.xml"
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
                            args "-v ${env.WORKSPACE}/settings.xml:/root/.m2/settings.xml"
                        }
                    }
                    steps {
                        dir('backend/backend') {
                            sh 'mvn package -DskipTests'
                        }
                    }
                }

                // =====================================================================
                // STAGE 5: 采用动态生成配置文件的终极方案
                // =====================================================================
                stage('5. Integration Tests') {
                    steps {
                        script {
                            try {
                                // ✅ 终极修复: 使用 sed 命令从原始文件中删除掉包含 "prometheus.yml" 的那一行，
                                // 然后将结果保存到一个全新的、干净的配置文件中。
                                echo 'Generating a CI-safe docker-compose file by removing the problematic volume mount...'
                                sh "sed '/prometheus.yml/d' docker-compose.yml > docker-compose.generated.yml"

                                // 打印生成的文件内容，作为最终验证
                                echo '--- Content of the dynamically generated docker-compose.generated.yml ---'
                                sh 'cat docker-compose.generated.yml'
                                echo '-------------------------------------------------------------------------'

                                // 让所有命令都只使用这个新生成的、绝对安全的文件
                                echo 'Ensuring a clean environment using the generated file...'
                                sh "docker-compose -f docker-compose.generated.yml down --remove-orphans"

                                echo 'Starting the application environment using the generated file...'
                                sh "docker-compose -f docker-compose.generated.yml up -d"
                                
                                echo 'Waiting for services to start (20 seconds)...' 
                                sleep(20)
                                echo 'Performing health check on port 8081...'
                                sh 'curl -f http://localhost:8081/actuator/health'

                            } finally {
                                echo 'Integration tests finished. Tearing down the environment...'
                                // 清理时也必须使用同一个生成的文件
                                sh "docker-compose -f docker-compose.generated.yml down"
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
