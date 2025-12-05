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
                            // 动态创建 settings.xml 文件来使用国内 Maven 镜像，解决网络问题
                            writeFile file: 'settings.xml', text: '''
                                <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
                                          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                                                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
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
                            // 使用 -s 参数告诉 Maven 配置文件的相对路径
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
                            // 复用上一步创建的 settings.xml 文件
                            sh 'mvn -s ../../settings.xml package -DskipTests'
                        }
                    }
                }

                stage('5. Integration Tests') {
                    steps {
                        script {
                            try {
                                // 终极修复方案: 动态生成一个 CI 专用的、安全的配置文件
                                // 1. 删除 prometheus 的 volumes 块，解决 Docker-in-Docker 路径问题
                                // 2. 将 prometheus 的端口从 9090 改为 9091，解决与 Jenkins 的端口冲突
                                echo 'Generating a CI-safe docker-compose file by removing volumes AND remapping ports...'
                                sh "sed '/prometheus:/,/networks:/ { /volumes:/,/prometheus.yml/d }' docker-compose.yml | sed 's/\"9090:9090\"/\"9091:9090\"/' > docker-compose.generated.yml"
                                
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
                                echo 'Tearing down the environment...'
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
            script {
                // 清理所有动态创建的临时文件，保持工作区干净
                if (fileExists('settings.xml')) {
                    echo 'Deleting dynamically created settings.xml...'
                    sh 'rm settings.xml'
                }
                if (fileExists('docker-compose.generated.yml')) {
                    echo 'Deleting dynamically created docker-compose.generated.yml...'
                    sh 'rm docker-compose.generated.yml'
                }
            }
            echo '收集测试报告...'
            junit allowEmptyResults: true, testResults: 'backend/backend/target/surefire-reports/*.xml'
        }
    }
}
