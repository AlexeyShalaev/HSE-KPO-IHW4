#Это так называемый мультистейдж docker-файл. Сначала делаем образ, который соберёт нам java-приложение,
#а затем из него берём собранный артефакт и кладём его в новый образ, чтобы оставить в нём меньше лишнего

# build
FROM maven:3.8.2-openjdk-17-slim as builder
# задаём переменную VERSION, которая будет использоваться для сборки проекта
ARG VERSION=0.0.1
WORKDIR /usr/src/app
COPY ./src ./src
COPY *.xml ./
RUN mvn package -Dversion.application=${VERSION} -DskipTests

# release
FROM openjdk:17-jdk-alpine
ARG VERSION=0.0.1
WORKDIR /app

# Установка curl
RUN apk add --no-cache curl

# создаём пользователя jaruser
RUN addgroup --system jaruser \
    && adduser -S -s /bin/false -G jaruser jaruser -D -H \
    && apk add --no-cache dumb-init
COPY --chown=jaruser:jaruser \
     --from=builder /usr/src/app/target/auth-service-${VERSION}.jar ./auth-service.jar
EXPOSE 8080
# приложение будет запускаться под пользователем jaruser
USER jaruser
ENTRYPOINT ["dumb-init", "java", "-jar", "-Dmyserver.bindPort=8080", "./auth-service.jar"]