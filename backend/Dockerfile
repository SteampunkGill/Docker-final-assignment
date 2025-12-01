# ⚠️ 修复点：把构建环境从 jdk-8 升级到 jdk-17
# 这样它就能听懂 "--release" 指令了，也能兼容你的新版 Maven 插件
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 1. 先复制依赖描述文件 (利用 Docker 缓存机制加速)
COPY ./backend/pom.xml .

# 2. 预下载依赖 (告诉 Maven 用阿里云镜像，否则会卡死)
# 注意：如果你的 pom.xml 里已经配置了阿里云，这里可以简化，但加上为了保险
RUN mvn dependency:go-offline -B

# 3. 复制所有源代码
COPY ./backend/src ./src

# 4. 打包项目，跳过测试 (为了省时间，测试我们在 CI/CD 环节做)
RUN mvn package -DskipTests

# ---- 阶段二：运行阶段 ----
# ⚠️ 修复点：运行环境也顺便升级到 Java 17，确保兼容性
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 从第一阶段把打好的包拿过来
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# 启动命令
CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]