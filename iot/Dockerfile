# 使用OpenJDK 17作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=prod

# 复制Maven配置文件
COPY pom.xml .
COPY iot-*/pom.xml ./iot-*/

# 复制源代码
COPY iot-*/src ./iot-*/src

# 安装Maven
RUN apt-get update && apt-get install -y maven

# 编译项目
RUN mvn clean package -DskipTests

# 复制构建的jar包
COPY iot-starter/target/iot-starter-*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/iot/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]