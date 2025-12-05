pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
        DOCKER_COMPOSE_PATH = "${env.WORKSPACE}/bin"
    }

    stages {
        stage('1. Setup Environment') { /* ... (保持不变) ... */ }

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
                            '''
                        }
                        // 进入子目录
                        dir('backend/backend') {
                            // 使用 -s 参数告诉 Maven 配置文件的位置
                            // 这个文件现在确实存在于 ../../settings.xml
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

                stage('6. Build & Push Docker Image') { /* ... (保持不变) ... */ }
                stage('7. Cleanup') { /* ... (保持不变) ... */ }
            }
        }
    }

    post {
        always {
            // ✅ 新增步骤: 在构建结束后，删除我们动态创建的临时文件，保持工作区干净
            deleteDir() // 清理工作区，或者用 sh 'rm settings.xml' 只删除特定文件
            echo '收集测试报告...'
            junit allowEmptyResults: true, testResults: 'backend/backend/target/surefire-reports/*.xml'
        }
    }
}
