# 使用OpenJDK 17作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制Maven配置文件
COPY pom.xml .

# 复制源代码
COPY src ./src

# 设置环境变量
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 暴露端口
EXPOSE 8080

# 构建应用
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

# 运行应用
CMD ["java", "-jar", "target/ddd-demo-1.0.0.jar"]