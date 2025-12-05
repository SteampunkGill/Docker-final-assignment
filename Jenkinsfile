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
                // STAGE 5: 采用动态创建 prometheus.yml 的终极方案
                // =====================================================================
                stage('5. Integration Tests') {
                    steps {
                        script {
                            try {
                                // ✅ 终极修复: 动态创建一个最小化的、有效的 prometheus.yml 文件
                                // 这样 docker-compose up 在挂载时就能找到一个真实的文件，而不是创建目录
                                echo 'Generating a dummy prometheus.yml for CI environment...'
                                writeFile file: 'prometheus.yml', text: '''
                                    global:
                                      scrape_interval: 15s
                                '''

                                // 现在我们可以安全地使用原始的 docker-compose.yml 文件了！
                                echo 'Ensuring a clean environment...'
                                sh "docker-compose down --remove-orphans"

                                echo 'Starting the application environment...'
                                sh "docker-compose up -d"
                                
                                echo 'Waiting for services to start (20 seconds)...' 
                                sleep(20)
                                echo 'Performing health check on port 8081...'
                                sh 'curl -f http://localhost:8081/actuator/health'

                            } finally {
                                echo 'Tearing down the environment...'
                                sh "docker-compose down"
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
            // ✅ 新增步骤: 清理我们动态创建的所有临时文件
            script {
                if (fileExists('settings.xml')) {
                    echo 'Deleting dynamically created settings.xml...'
                    sh 'rm settings.xml'
                }
                if (fileExists('prometheus.yml')) {
                    echo 'Deleting dynamically created prometheus.yml...'
                    sh 'rm prometheus.yml'
                }
            }
            echo '收集测试报告...'
            junit allowEmptyResults: true, testResults: 'backend/backend/target/surefire-reports/*.xml'
        }
    }
}
