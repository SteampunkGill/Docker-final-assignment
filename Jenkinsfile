pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
    }

    stages {
        stage('0. 详细检查项目结构') {
            steps {
                echo '深度检查项目结构...'
                sh '''
                    echo "=== Workspace 路径 ==="
                    pwd
                    
                    echo ""
                    echo "=== backend/ 内容 ==="
                    ls -laR backend/ | head -100
                    
                    echo ""
                    echo "=== backend/backend/ 详细内容 ==="
                    if [ -d "backend/backend" ]; then
                        ls -la backend/backend/
                        
                        echo ""
                        echo "=== 检查 pom.xml 是否存在 ==="
                        if [ -f "backend/backend/pom.xml" ]; then
                            echo "✅ pom.xml 存在"
                            echo "文件大小:"
                            ls -lh backend/backend/pom.xml
                            echo ""
                            echo "pom.xml 前10行:"
                            head -10 backend/backend/pom.xml
                        else
                            echo "❌ pom.xml 不存在"
                        fi
                        
                        echo ""
                        echo "=== 检查 src 目录 ==="
                        if [ -d "backend/backend/src" ]; then
                            echo "✅ src 目录存在"
                            ls -la backend/backend/src/
                        else
                            echo "❌ src 目录不存在"
                        fi
                    else
                        echo "❌ backend/backend 目录不存在"
                    fi
                    
                    echo ""
                    echo "=== 完整的文件树 ==="
                    find backend/ -type f | head -30
                '''
            }
        }

        stage('1. Run Unit Tests') {
            steps {
                echo '运行单元测试...'
                script {
                    // 使用绝对路径
                    def backendPath = "${WORKSPACE}/backend/backend"
                    echo "Backend 路径: ${backendPath}"
                    
                    sh """
                        echo "验证路径: ${backendPath}"
                        ls -la ${backendPath}
                        
                        echo ""
                        echo "开始运行 Maven 测试..."
                        docker run --rm \
                            -v ${backendPath}:/app \
                            -w /app \
                            maven:3.9-eclipse-temurin-17 \
                            bash -c "ls -la /app && mvn test"
                    """
                }
            }
        }

        stage('2. Build & Package') {
            steps {
                echo '打包 Spring Boot 应用...'
                script {
                    def backendPath = "${WORKSPACE}/backend/backend"
                    
                    sh """
                        docker run --rm \
                            -v ${backendPath}:/app \
                            -w /app \
                            maven:3.9-eclipse-temurin-17 \
                            bash -c "ls -la /app && mvn clean package -DskipTests"
                    """
                }
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
                            
                            # 在 backend 目录构建
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
            echo '清理构建缓存...'
            sh 'docker builder prune -f || true'
        }
        success {
            echo '✅ Pipeline 执行成功！'
            echo "镜像: ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}"
        }
        failure {
            echo '❌ Pipeline 执行失败，请检查日志'
        }
    }
}
