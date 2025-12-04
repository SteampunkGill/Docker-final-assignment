pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
    }

    stages {
        stage('0. 验证项目结构') {
            steps {
                echo '检查项目结构...'
                sh '''
                    echo "=== Workspace 路径 ==="
                    pwd
                    
                    echo ""
                    echo "=== backend 目录内容 ==="
                    ls -la backend/
                    
                    echo ""
                    echo "=== 查找 pom.xml ==="
                    find backend/ -name "pom.xml" -type f
                    
                    echo ""
                    echo "=== 查找 Dockerfile ==="
                    find backend/ -name "Dockerfile" -type f
                '''
            }
        }

        stage('1. Run Unit Tests') {
            steps {
                echo '运行单元测试...'
                // 挂载嵌套的 backend 目录
                sh '''
                    docker run --rm \
                        -v ${WORKSPACE}/backend/backend:/app \
                        -w /app \
                        maven:3.9-eclipse-temurin-17 \
                        mvn test
                '''
            }
        }

        stage('2. Build & Package') {
            steps {
                echo '打包 Spring Boot 应用...'
                sh '''
                    docker run --rm \
                        -v ${WORKSPACE}/backend/backend:/app \
                        -w /app \
                        maven:3.9-eclipse-temurin-17 \
                        mvn clean package -DskipTests
                '''
            }
        }

        stage('3. Build & Push Docker Image') {
            steps {
                echo "构建并推送 Docker 镜像: ${BACKEND_IMAGE_NAME}"
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'DOCKERHUB_CREDENTIALS', 
                        passwordVariable: 'DOCKERHUB_PASSWORD', 
                        usernameVariable: 'DOCKERHUB_USER'
                    )]) {
                        sh """
                            echo ${DOCKERHUB_PASSWORD} | docker login -u ${DOCKERHUB_USER} --password-stdin
                            
                            # 在 backend 目录下构建镜像
                            cd backend
                            docker build -t ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} .
                            docker tag ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} ${BACKEND_IMAGE_NAME}:latest
                            
                            docker push ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}
                            docker push ${BACKEND_IMAGE_NAME}:latest
                        """
                    }
                }
            }
        }

        stage('4. Cleanup') {
            steps {
                echo '清理本地镜像...'
                sh """
                    docker rmi ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} || true
                    docker rmi ${BACKEND_IMAGE_NAME}:latest || true
                    docker logout
                """
            }
        }
    }

    post {
        always {
            echo '清理工作空间...'
            // 清理 Maven 缓存（可选）
            sh 'docker system prune -f || true'
        }
        success {
            echo '✅ Pipeline 执行成功！'
            echo "镜像已推送: ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}"
            echo "镜像标签: ${BACKEND_IMAGE_NAME}:latest"
        }
        failure {
            echo '❌ Pipeline 执行失败，请检查日志'
        }
    }
}
