pipeline {
    agent any

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

        // ✅ 关键修改：添加 reuseNode true
        stage('2. Run Unit Tests') {
            agent {
                docker { 
                    image 'maven:3.9-eclipse-temurin-17'
                    reuseNode true  // 👈 复用同一个工作区，避免路径问题
                }
            }
            steps {
                echo '运行单元测试...'
                dir('backend/backend') {
                    sh 'mvn test'
                }
            }
        }

        // ✅ 同样添加 reuseNode true
        stage('3. Build & Package') {
            agent {
                docker { 
                    image 'maven:3.9-eclipse-temurin-17'
                    reuseNode true  // 👈 复用同一个工作区
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
            // ✅ 修复：使用 allowEmptyResults，避免没有测试文件时报错
            junit allowEmptyResults: true, testResults: 'backend/backend/target/surefire-reports/*.xml'
        }
    }
}
