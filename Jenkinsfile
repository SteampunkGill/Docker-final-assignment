pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
        // Jenkins 会自动处理路径分隔符，所以 '/' 通常也能工作，但定义为 bin 没问题
        DOCKER_COMPOSE_PATH = "${env.WORKSPACE}\\bin"
    }

    stages {
        // =====================================================================
        // STAGE 1: 为 Windows 环境重写的 Setup Stage
        // =====================================================================
        stage('1. Setup Environment') {
            steps {
                script {
                    echo "检查并准备 docker-compose for Windows..."
                    
                    // 使用 bat 步骤和 Windows 的 `if not exist` 语法
                    bat "if not exist \\"%DOCKER_COMPOSE_PATH%\\docker-compose.exe\\" (" +
                        "echo docker-compose.exe not found, downloading... && " +
                        "mkdir %DOCKER_COMPOSE_PATH% && " +
                        // 下载 Windows 版本的 docker-compose.exe
                        "curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-windows-x86_64.exe -o %DOCKER_COMPOSE_PATH%\\docker-compose.exe" +
                    ") else (" +
                        "echo docker-compose.exe already exists." +
                    ")"
                    
                    echo "验证 docker-compose 版本..."
                    // 使用 bat 步骤执行 .exe 文件
                    bat "\\"%DOCKER_COMPOSE_PATH%\\docker-compose.exe\\" --version"
                }
            }
        }

        stage('Build, Test & Push') {
            environment {
                // ✅ 关键修改: 使用分号 ; 作为 Windows 的 PATH 分隔符
                PATH = "${DOCKER_COMPOSE_PATH};${env.PATH}"
            }
            stages {
                stage('2. Checkout Code') {
                    steps {
                        echo '拉取最新的代码...'
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
                        echo '运行单元测试...'
                        dir('backend/backend') {
                            // 'mvn' 命令通常是跨平台的
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
                                // 'docker-compose' 现在可以在 PATH 中被找到
                                sh 'docker-compose up -d'
                                
                                echo '等待服务启动 (等待20秒)...' 
                                sleep(20)

                                echo '执行健康检查...'
                                sh 'curl -f http://localhost:8080/actuator/health'

                            } finally {
                                echo '集成测试完成，关闭应用环境...'
                                sh 'docker-compose down'
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
                            // 'docker' 命令通常是跨平台的
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
                        echo '清理工作...'
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
