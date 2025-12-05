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

                // =====================================================================
                // STAGE 5: 增加了强制清理端口的终极方案
                // =====================================================================
                stage('5. Integration Tests') {
                    steps {
                        script {
                            try {
                                // ✅ 终极修复: 在所有操作之前，强制删除可能占用端口的旧容器
                                // 查找任何正在使用 9090 端口的容器并强制删除它
                                echo 'Force removing any container using port 9090...'
                                sh 'docker ps -q --filter "publish=9090" | xargs -r docker rm -f || true'

                                echo 'Generating a CI-safe docker-compose file by REMOVING the prometheus service and its volume block...'
                                // 修正后的 sed 命令：
                                // 1. 找到以 'prometheus:' 开头的行。
                                // 2. 匹配到以 'networks:' 开头的行（这是 Prometheus 服务的结束）。
                                // 3. 删除从 'prometheus:' 到 'networks:' 的整个块。
                                // 注意：这里假设 prometheus 服务是 docker-compose.yml 中一个独立的顶级服务定义。
                                // 如果 prometheus 服务定义在其他服务内部，或者结构更复杂，可能需要更精确的 sed 表达式。
                                sh "sed '/prometheus:/,/networks:/d' docker-compose.yml > docker-compose.generated.yml"

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
