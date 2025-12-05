pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
        // 使用正斜杠是更安全的做法，Jenkins 会在 agent 上将其正确转换为 Windows 路径
        DOCKER_COMPOSE_PATH = "${env.WORKSPACE}/bin"
    }

    stages {
        stage('1. Setup Environment') {
            steps {
                script {
                    echo "Checking and preparing docker-compose for Windows..."
                    
                    // ✅ 关键修复: 使用三个单引号 (''') 来包裹整个 bat 命令块
                    // 这可以防止 Groovy 错误地解析反斜杠 '\'
                    bat '''
                        if not exist "%DOCKER_COMPOSE_PATH%\\docker-compose.exe" (
                            echo docker-compose.exe not found, downloading...
                            mkdir "%DOCKER_COMPOSE_PATH%"
                            curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-windows-x86_64.exe -o "%DOCKER_COMPOSE_PATH%\\docker-compose.exe"
                        ) else (
                            echo docker-compose.exe already exists.
                        )
                    '''
                    
                    echo "Verifying docker-compose version..."
                    // 单行命令也可以用单引号 ''
                    bat '"%DOCKER_COMPOSE_PATH%\\docker-compose.exe" --version'
                }
            }
        }

        stage('Build, Test & Push') {
            environment {
                // 使用分号 ; 作为 Windows 的 PATH 分隔符
                PATH = "${DOCKER_COMPOSE_PATH};${env.PATH}"
            }
            stages {
                stage('2. Checkout Code') {
                    steps {
                        echo '拉取最新的代码...'
                        checkout scm
                    }
                }

                // 这个 stage 在 Docker 容器内运行，所以必须用 sh
                stage('3. Run Unit Tests') {
                    agent {
                        docker { 
                            image 'maven:3.9-eclipse-temurin-17'
                            reuseNode true
                        }
                    }
                    steps {
                        echo '运行单元测试...'
                        dir('backend/backend') {
                            sh 'mvn test'
                        }
                    }
                }

                // 这个 stage 在 Docker 容器内运行，所以必须用 sh
                stage('4. Build & Package') {
                    agent {
                        docker { 
                            image 'maven:3.9-eclipse-temurin-17'
                            reuseNode true
                        }
                    }
                    steps {
                        echo '打包 Spring Boot 应用...'
                        dir('backend/backend') {
                            sh 'mvn package -DskipTests'
                        }
                    }
                }

                stage('5. Integration Tests') {
                    steps {
                        script {
                            try {
                                echo '启动完整的应用环境进行集成测试...'
                                // ✅ 关键修复: 在 Windows agent 上使用 bat
                                bat 'docker-compose up -d'
                                
                                echo '等待服务启动 (等待20秒)...' 
                                sleep(20)

                                echo '执行健康检查...'
                                bat 'curl -f http://localhost:8080/actuator/health'

                            } finally {
                                echo '集成测试完成，关闭应用环境...'
                                bat 'docker-compose down'
                            }
                        }
                    }
                }

                stage('6. Build & Push Docker Image') {
                    steps {
                        echo "构建并推送 Docker 镜像: ${BACKEND_IMAGE_NAME}"
                        withCredentials([usernamePassword(
                            credentialsId: 'DOCKERHUB_CREDENTIALS', 
                            passwordVariable: 'DOCKERHUB_PASSWORD', 
                            usernameVariable: 'DOCKERHUB_USERNAME'
                        )]) {
                            // ✅ 关键修复: 在 Windows agent 上使用 bat
                            bat "docker login -u ${DOCKERHUB_USERNAME} -p ${DOCKERHUB_PASSWORD}"
                            bat "docker build -t ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} -f backend/Dockerfile backend"
                            bat "docker tag ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} ${BACKEND_IMAGE_NAME}:latest"
                            bat "docker push ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}"
                            bat "docker push ${BACKEND_IMAGE_NAME}:latest"
                        }
                    }
                }

                stage('7. Cleanup') {
                    steps {
                        echo '清理工作...'
                        // ✅ 关键修复: 在 Windows agent 上使用 bat
                        bat 'docker logout'
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
