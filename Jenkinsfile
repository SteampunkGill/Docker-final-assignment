// ==========================================================
//                 这是最终、正确的 Jenkinsfile
// ==========================================================
pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'steampunkgill'
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend"
    }

    stages {
        stage('1. Run Unit Tests') {
            steps {
                echo '运行单元测试...'
                // --- 修正点 1: 在容器内先进入 backend 子目录，再执行 mvn 命令 ---
                sh 'docker run --rm -v ${WORKSPACE}/backend:/app -w /app maven:3.9-eclipse-temurin-17 sh -c "cd backend && mvn test"'
            }
        }

        stage('2. Build & Package') {
            steps {
                echo '打包 Spring Boot 应用...'
                // --- 修正点 2: 打包时也一样，先进入 backend 子目录 ---
                sh 'docker run --rm -v ${WORKSPACE}/backend:/app -w /app maven:3.9-eclipse-temurin-17 sh -c "cd backend && mvn package -DskipTests"'
            }
        }

        stage('3. Integration Tests') {
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

        stage('4. Build & Push Docker Image') {
            steps {
                echo "构建并推送 Docker 镜像: ${BACKEND_IMAGE_NAME}"
                withCredentials([usernamePassword(credentialsId: 'DOCKERHUB_CREDENTIALS', passwordVariable: 'DOCKERHUB_PASSWORD', usernameVariable: 'DOCKERHUB_USERNAME')]) {
                    sh "docker login -u ${DOCKERHUB_USERNAME} -p ${DOCKERHUB_PASSWORD}"
                    sh "docker build -t ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} -f backend/Dockerfile ."
                    sh "docker tag ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} ${BACKEND_IMAGE_NAME}:latest"
                    sh "docker push ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}"
                    sh "docker push ${BACKEND_IMAGE_NAME}:latest"
                }
            }
        }

        stage('5. Cleanup') {
            steps {
                echo '清理工作...'
                sh 'docker logout'
            }
        }
    }

    post {
        always {
            echo '收集测试报告...'
            // --- 修正点 3: 报告的路径也需要指向正确的 backend 子目录 ---
            junit 'backend/backend/target/surefire-reports/*.xml'
        }
    }
}
