FROM adoptopenjdk:11-hotspot AS builder
ENV USE_PROFILE local
ENV SPRING_CONFIG_ACTIVATE_ON_PROFILE local
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

ARG NOW=$(date +"%Y%m%d")

FROM adoptopenjdk:11-hotspot
COPY --from=builder build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", \
            "-Dspring.profiles.active=${USE_PROFILE}", \
            "/app.jar", \
            "--spring.batch.job.name=subscriptionJob", \
            "date=${NOW}"]
