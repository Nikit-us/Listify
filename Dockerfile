FROM gradle:jdk21-alpine AS build

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle . .

RUN gradle build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache openssl

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080