FROM gradle:8.8.0-jdk21-jammy AS build

WORKDIR /home/gradle/project

COPY --chown=gradle:gradle . .

RUN gradle build --no-daemon -x test


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --system appuser && useradd --system --gid appuser appuser

RUN chown -R appuser:appuser /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

USER appuser

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]