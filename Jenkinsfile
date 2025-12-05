pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
        // 定义 docker-compose 的下载路径，放在工作区内的一个 bin 目录中
        DOCKER_COMPOSE_PATH = "${env.WORKSPACE}/bin"
    }

    stages {
        stage('1. Setup Environment') {
            steps {
                script {
                    echo "检查并准备 docker-compose..."
                    // 检查我们定义的路径下 docker-compose 是否存在
                    if (!fileExists("${DOCKER_COMPOSE_PATH}/docker-compose")) {
                        echo "docker-compose 未找到，开始下载..."
                        // 创建目录
                        sh "mkdir -p ${DOCKER_COMPOSE_PATH}"
                        // 使用 curl 从 GitHub 下载最新稳定版，并放到指定目录
                        sh "curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o ${DOCKER_COMPOSE_PATH}/docker-compose"
                        // 赋予执行权限
                        sh "chmod +x ${DOCKER_COMPOSE_PATH}/docker-compose"
                    } else {
                        echo "docker-compose 已存在，跳过下载。"
                    }
                    // 验证一下版本
                    sh "${DOCKER_COMPOSE_PATH}/docker-compose --version"
                }
            }
        }

        // ✅ 关键修改: 将后续所有需要 docker-compose 的 stage 包裹在 withEnv 中
        stage('Build, Test & Push') {
            // 使用 withEnv 将我们下载的 docker-compose 所在目录加入 PATH
            environment {
                PATH = "${DOCKER_COMPOSE_PATH}:${env.PATH}"
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
                                // 现在这个命令可以被正确找到了
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
