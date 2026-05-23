# 管理端 API：由 build-and-save 脚本将 fat jar 拷入本目录后再构建（构建上下文为上级 docker/）
# 默认走 DaoCloud 代理 Docker Hub 官方库；境外可加参数：--build-arg BASE_IMAGE=eclipse-temurin:21-jre-alpine
ARG BASE_IMAGE=docker.m.daocloud.io/library/eclipse-temurin:21-jre-alpine
FROM ${BASE_IMAGE}

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

ARG JAR_FILE=zesheng-admin-service-0.0.1-SNAPSHOT.jar
COPY admin/${JAR_FILE} app.jar
RUN chown app:app app.jar

USER app

EXPOSE 9068

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 默认 profile 由 Jar 内 application.yml 指定（prod）；Compose 可通过 SPRING_PROFILES_ACTIVE 覆盖
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
