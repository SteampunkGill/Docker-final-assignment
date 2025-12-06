pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
        DOCKER_COMPOSE_PATH = "${env.WORKSPACE}/bin"
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
                        script {
                            writeFile file: 'settings.xml', text: '''
                                <settings>
                                  <mirrors>
                                    <mirror>
                                      <id>aliyunmaven</id>
                                      <mirrorOf>*</mirrorOf>
                                      <name>Alibaba Cloud Maven Mirror</name>
                                      <url>https://maven.aliyun.com/repository/public</url>
                                    </mirror>
                                  </mirrors>
                                </settings>
                            '''
                        }
                        dir('backend/backend') {
                            sh 'mvn -s ../../settings.xml test'
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
                            sh 'mvn -s ../../settings.xml package -DskipTests'
                        }
                    }
                }

                stage('5. Integration Tests') {
                    steps {
                        script {
                            try {
                                echo 'Force cleaning up all ports for a truly resilient environment...'
                                sh 'docker ps -q --filter "publish=8081" | xargs -r docker rm -f || true'
                                sh 'docker ps -q --filter "publish=80"   | xargs -r docker rm -f || true'
                                sh 'docker ps -q --filter "publish=3307" | xargs -r docker rm -f || true'
                                sh 'docker ps -q --filter "publish=9091" | xargs -r docker rm -f || true'

                                echo 'Generating a CI-safe docker-compose file...'
                                sh "sed '/prometheus:/,/networks:/ { /volumes:/,/prometheus.yml/d }' docker-compose.yml | sed 's/\"9090:9090\"/\"9091:9090\"/' > docker-compose.generated.yml"

                                echo 'Starting the application environment using the generated file...'
                                sh "docker-compose -f docker-compose.generated.yml up -d"

                                echo 'Waiting for the backend service to become healthy...'
                                // --- 👇 FIX: 将 [[ ]] 改为 [ ]，以兼容所有 sh 环境 ---
                                sh '''
                                    set +x
                                    echo "Pinging http://backend:8081/actuator/health ..."

                                    timeout=90
                                    while [ "$(docker run --network=docker-ecommerce-pipeline_my-app-network --rm curlimages/curl -s -o /dev/null -w ''%{http_code}'' http://backend:8081/actuator/health)" != "200" ]; do
                                        if [ $timeout -eq 0 ]; then
                                            echo "Timeout: Backend service did not become healthy in 90 seconds."
                                            exit 1
                                        fi

                                        echo -n "."
                                        sleep 5
                                        timeout=$((timeout - 5))
                                    done

                                    echo -e "\\nBackend service is healthy!"
                                    set -x
                                '''

                            } finally {
                                echo 'Tearing down the environment...'
                                sh "docker-compose -f docker-compose.generated.yml down --remove-orphans || true"
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
                            // --- 👇 FIX: 添加 retry 块，失败后自动重试3次 ---
                            retry(3) {
                                sh "docker login -u ${DOCKERHUB_USERNAME} -p ${DOCKERHUB_PASSWORD}"
                            }
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
            script {
                if (fileExists('settings.xml')) {
                    sh 'rm settings.xml'
                }
                if (fileExists('docker-compose.generated.yml')) {
                    sh 'rm docker-compose.generated.yml'
                }
            }
            echo '收集测试报告...'
            junit allowEmptyResults: true, testResults: 'backend/backend/target/surefire-reports/*.xml'
        }
    }
}