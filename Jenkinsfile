pipeline {
    agent any

    // ✅ 关键修改 #1: 声明需要使用 Jenkins 全局工具中配置的 docker-compose
    tools {
        org.jenkinsci.plugins.docker.compose.DockerComposeTool 'docker-compose-latest' // 确保这里的名字和你 Jenkins UI 配置的一致
    }

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
    }

    stages {
        stage('1. Checkout Code') {
            steps {
                echo '拉取最新的代码...'
                checkout scm
            }
        }

        stage('2. Run Unit Tests') {
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

        stage('3. Build & Package') {
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

        stage('4. Integration Tests') {
            steps {
                script {
                    try {
                        echo '启动完整的应用环境进行集成测试...'
                        // 现在这个命令可以在 agent 上被正确找到了
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

        stage('5. Build & Push Docker Image') {
            steps {
                echo "构建并推送 Docker 镜像: ${BACKEND_IMAGE_NAME}"
                withCredentials([usernamePassword(
                    credentialsId: 'DOCKERHUB_CREDENTIALS', 
                    passwordVariable: 'DOCKERHUB_PASSWORD', 
                    usernameVariable: 'DOCKERHUB_USERNAME'
                )]) {
                    sh "docker login -u ${DOCKERHUB_USERNAME} -p ${DOCKERHUB_PASSWORD}"
                    sh "docker build -t ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} -f backend/Dockerfile ."
                    sh "docker tag ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} ${BACKEND_IMAGE_NAME}:latest"
                    sh "docker push ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}"
                    sh "docker push ${BACKEND_IMAGE_NAME}:latest"
                }
            }
        }

        stage('6. Cleanup') {
            steps {
                echo '清理工作...'
                sh 'docker logout'
            }
        }
    }

    post {
        always {
            echo '收集测试报告...'
            // ✅ 关键修改 #2: (你已经做对了) 允许没有测试报告文件
            junit allowEmptyResults: true, testResults: 'backend/backend/target/surefire-reports/*.xml'
        }
    }
}
