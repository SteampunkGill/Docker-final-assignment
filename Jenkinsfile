pipeline {
    agent any // 任何可用的 Jenkins Agent 都可以运行此流水线

    // 定义全局环境变量，方便在整个流水线中使用
    environment {
        DOCKERHUB_USERNAME = 'steampunkgill' // Docker Hub 用户名
        BACKEND_IMAGE_NAME = "${DOCKERHUB_USERNAME}/docker-ecommerce-backend" // 后端 Docker 镜像名称
        DOCKER_COMPOSE_PATH = "${env.WORKSPACE}/bin" // docker-compose 二进制文件的存放路径
    }

    // 定义流水线的各个阶段
    stages {
        stage('1. Setup Environment') {
            steps {
                script {
                    echo "Checking and preparing docker-compose for Linux..."
                    // 检查并下载 docker-compose，如果不存在则下载，并赋予执行权限
                    sh '''
                        if [ ! -f "${DOCKER_COMPOSE_PATH}/docker-compose" ]; then
                            echo "docker-compose not found, downloading..."
                            mkdir -p "${DOCKER_COMPOSE_PATH}"
                            # 下载最新版本的 docker-compose
                            curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o "${DOCKER_COMPOSE_PATH}/docker-compose"
                            chmod +x "${DOCKER_COMPOSE_PATH}/docker-compose"
                        else
                            echo "docker-compose already exists."
                        fi
                    '''
                    echo "Verifying docker-compose version..."
                    // 验证 docker-compose 版本
                    sh "'${DOCKER_COMPOSE_PATH}/docker-compose' --version"
                }
            }
        }

        stage('Build, Test & Push') {
            // 为此阶段及其子阶段设置 PATH 环境变量，确保 docker-compose 可用
            environment {
                PATH = "${DOCKER_COMPOSE_PATH}:${env.PATH}"
            }
            stages {
                stage('2. Checkout Code') {
                    steps {
                        echo "Checking out source code..."
                        // 从 SCM（如 Git）检出代码
                        checkout scm
                    }
                }

                // =====================================================================
                // STAGE 3 & 4: 采用动态创建 settings.xml 的终极方案
                // 这种方法在 Jenkins 工作区创建 settings.xml，然后 Maven 通过 -s 参数引用，
                // 避免了 Docker 卷挂载的复杂性，且确保了 Maven 配置的一致性。
                // =====================================================================
                stage('3. Run Unit Tests') {
                    // 使用 Maven Docker 镜像作为代理，提供独立的构建环境
                    agent {
                        docker {
                            image 'maven:3.9-eclipse-temurin-17' // 指定 Maven 镜像版本
                            reuseNode true // 尝试重用 Docker 容器，提高效率
                        }
                    }
                    steps {
                        // ✅ 终极修复: 在执行 mvn 命令之前，先动态创建 settings.xml 文件
                        script {
                            echo "Dynamically creating Maven settings.xml..."
                            // 使用 writeFile 步骤在 Jenkins 工作区根目录创建一个临时的 settings.xml
                            writeFile file: 'settings.xml', text: '''
                                <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
                                          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                                                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
                                  <mirrors>
                                    <mirror>
                                      <id>aliyunmaven</id>
                                      <mirrorOf>*</mirrorOf>
                                      <name>Alibaba Cloud Maven Mirror</name>
                                      <url>https://maven.aliyun.com/repository/public</url>
                                    </mirror>
                                  </mirrors>
                                </settings>
                            '''.stripIndent() // 使用 stripIndent() 清除多行字符串中多余的缩进
                        }
                        // 进入后端项目的子目录
                        dir('backend/backend') {
                            echo "Running unit tests with custom settings.xml..."
                            // 使用 -s 参数告诉 Maven settings.xml 文件的位置
                            // ../../settings.xml 是相对于当前 'backend/backend' 目录的路径
                            sh 'mvn -s ../../settings.xml test'
                        }
                    }
                }

                stage('4. Build & Package') {
                    // 同样使用 Maven Docker 镜像
                    agent {
                        docker {
                            image 'maven:3.9-eclipse-temurin-17'
                            reuseNode true
                        }
                    }
                    steps {
                        // 这个阶段不需要再次创建 settings.xml，因为它在上一个阶段已经创建并存在于工作区根目录
                        dir('backend/backend') {
                            echo "Building and packaging application with custom settings.xml..."
                            // 同样使用 -s 参数指定 settings.xml，并跳过测试 (-DskipTests)
                            sh 'mvn -s ../../settings.xml package -DskipTests'
                        }
                    }
                }

                // Stage 5 已经使用了动态生成文件的终极方案，保持不变
                stage('5. Integration Tests') {
                    steps {
                        script {
                            try {
                                echo 'Generating a CI-safe docker-compose file by excluding prometheus.yml...'
                                // 使用 sed 命令从 docker-compose.yml 中移除对 prometheus.yml 的引用，
                                // 生成一个适用于 CI 环境的 docker-compose 文件
                                sh "sed '/prometheus.yml/d' docker-compose.yml > docker-compose.generated.yml"

                                echo 'Ensuring a clean environment before starting integration tests...'
                                // 停止并移除所有由 docker-compose.generated.yml 定义的服务及其关联的匿名卷
                                sh "docker-compose -f docker-compose.generated.yml down --remove-orphans"

                                echo 'Starting the application environment for integration tests...'
                                // 在后台启动由 docker-compose.generated.yml 定义的服务
                                sh "docker-compose -f docker-compose.generated.yml up -d"

                                echo 'Waiting for services to start (20 seconds)...'
                                sleep(20) // 给予服务启动的时间

                                echo 'Performing health check on port 8081...'
                                // 对应用程序的健康检查端点进行 curl 请求，-f 参数表示如果 HTTP 状态码表示错误则失败
                                sh 'curl -f http://localhost:8081/actuator/health'
                            } finally {
                                echo 'Tearing down the integration test environment...'
                                // 无论集成测试成功与否，都停止并移除服务，确保环境清理
                                sh "docker-compose -f docker-compose.generated.yml down"
                            }
                        }
                    }
                }

                stage('6. Build & Push Docker Image') {
                    steps {
                        echo "Logging into Docker Hub..."
                        // 使用 Jenkins 凭据管理插件安全地获取 Docker Hub 凭据
                        withCredentials([usernamePassword(
                            credentialsId: 'DOCKERHUB_CREDENTIALS', // Jenkins 中配置的凭据 ID
                            passwordVariable: 'DOCKERHUB_PASSWORD', // 密码变量名
                            usernameVariable: 'DOCKERHUB_USERNAME' // 用户名变量名
                        )]) {
                            // 使用获取到的凭据登录 Docker Hub
                            sh "docker login -u ${DOCKERHUB_USERNAME} -p ${DOCKERHUB_PASSWORD}"
                            echo "Building Docker image: ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}..."
                            // 构建 Docker 镜像，使用当前 Jenkins 构建号作为标签
                            sh "docker build -t ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} -f backend/Dockerfile backend"
                            echo "Tagging Docker image as latest..."
                            // 为镜像添加 'latest' 标签
                            sh "docker tag ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} ${BACKEND_IMAGE_NAME}:latest"
                            echo "Pushing Docker image ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER} to Docker Hub..."
                            // 推送带构建号标签的镜像
                            sh "docker push ${BACKEND_IMAGE_NAME}:${BUILD_NUMBER}"
                            echo "Pushing Docker image ${BACKEND_IMAGE_NAME}:latest to Docker Hub..."
                            // 推送带 'latest' 标签的镜像
                            sh "docker push ${BACKEND_IMAGE_NAME}:latest"
                        }
                    }
                }

                stage('7. Cleanup') {
                    steps {
                        echo 'Logging out from Docker Hub...'
                        // 从 Docker Hub 注销
                        sh 'docker logout'
                    }
                }
            }
        }
    }

    // 流水线执行后的后置操作
    post {
        always {
            echo 'Collecting test reports...'
            // 收集 Maven Surefire 插件生成的单元测试报告
            // allowEmptyResults: true 表示即使没有找到测试报告也不会导致构建失败
            junit allowEmptyResults: true, testResults: 'backend/backend/target/surefire-reports/*.xml'

            // ✅ 新增步骤: 在构建结束后，删除我们动态创建的临时文件，保持工作区干净
            script {
                echo 'Cleaning up dynamically created files...'
                // 检查并删除动态创建的 settings.xml 文件
                if (fileExists('settings.xml')) {
                    sh 'rm settings.xml'
                    echo 'Removed settings.xml.'
                }
                // 检查并删除动态生成的 docker-compose.generated.yml 文件
                if (fileExists('docker-compose.generated.yml')) {
                    sh 'rm docker-compose.generated.yml'
                    echo 'Removed docker-compose.generated.yml.'
                }
            }
        }
    }
}
