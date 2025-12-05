// ==========================================================
//                 这是最终、绝对正确的 Jenkinsfile
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
                // --- 最终修正: 给 sh -c 后面的命令加上双引号 ---
                sh 'docker run --rm -v ${WORKSPACE}/backend:/app -w /app maven:3.9-eclipse-temurin-17 sh -c "cd backend && mvn test"'
            }
        }

        stage('2. Build & Package') {
            steps {
                echo '打包 Spring Boot 应用...'
                // --- 最终修正: 这里也一样，加上双引号 ---
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
                        sh 'curl -f http://localhost:8080/actuator/health
