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

                // =====================================================================
                // STAGE 3 & 4: 采用动态创建 settings.xml 的终极方案
                // =====================================================================
                stage('3. Run Unit Tests') {
                    agent {
                        docker {
                            image 'maven:3.9-eclipse-temurin-17'
                            reuseNode true
                        }
                    }
                    steps {
                        // ✅ 终极修复: 在执行 mvn 之前，先动态创建 settings.xml 文件
                        script {
                            // 使用 writeFile 步骤在工作区根目录创建一个临时的 settings.xml
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
                            '''.stripIndent() // 使用 stripIndent() 清除多余的缩进
                        }
                        // 进入子目录
                        dir('backend/backend') {
                            // 使用 -s 参数告诉 Maven 配置文件的位置
                            // 这个文件现在确实存在于 ../../settings.xml (相对于当前 dir 的路径)
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
                        // 这个阶段不需要再次创建文件，因为它在上一个阶段已经创建好了
                        dir('backend/backend') {
                            sh 'mvn -s ../../settings.xml package -DskipTests'
                        }
                    }
                }

                // Stage 5 保持我们之前成功的动态生成方案
                stage('5. Integration Tests') {
                    steps {
                        script {
                            try {
                                echo 'Generating a CI-safe docker-compose file...'
                                sh "sed '/prometheus.yml/d' docker-compose.yml > docker-compose.generated.yml"
                                echo 'Ensuring a clean environment...'
                                sh "docker-compose -f docker-compose.generated.yml down --remove-orphans"
                                echo 'Starting the application environment...'
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
            echo '收集测试报告...'
            junit allowEmptyResults: true, testResults: 'backend/backend/target/surefire-reports/*.xml'

            // ✅ 新增步骤: 在构建结束后，删除我们动态创建的临时文件，保持工作区干净
            // 注意：deleteDir() 会删除整个工作区，如果只想删除 settings.xml，请使用 sh 'rm settings.xml'
            // 在 post 块中，通常会执行一些清理操作，如果希望每次构建都从一个干净的工作区开始，使用 deleteDir() 是可以的。
            // 如果希望保留某些文件，则需要更精细的删除命令。
            script {
                if (fileExists('settings.xml')) {
                    echo 'Deleting dynamically created settings.xml...'
                    sh 'rm settings.xml'
                }
                // 也可以考虑删除 docker-compose.generated.yml
                if (fileExists('docker-compose.generated.yml')) {
                    echo 'Deleting dynamically created docker-compose.generated.yml...'
                    sh 'rm docker-compose.generated.yml'
                }
            }
        }
    }
}
