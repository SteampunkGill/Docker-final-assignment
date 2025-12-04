groovy
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
                sh 'docker run --rm -v ${WORKSPACE}/backend:/app -w /app maven:3.9-eclipse-temurin-17 mvn test'
            }
        }

        stage('2. Build & Package') {
            steps {
                echo '打包 Spring Boot 应用...'
                sh 'docker run --rm -v ${WORKSPACE}/backend:/app -w /app maven:3.9-eclipse-temurin-17 mvn package -DskipTests'
            }
        }

        stage('3. Build & Push Docker Image') {
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

        stage('4. Cleanup') {
            steps {
                echo '清理工作...'
                sh 'docker logout'
            }
        }
    }
}