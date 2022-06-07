FROM eclipse-temurin:17-jdk-alpine

CMD ["/opt/java/openjdk/bin/java", "-jar", "/opt/ssdc-rh-service.jar"]

RUN addgroup --gid 1000 rhservice && \
    adduser --system --uid 1000 rhservice rhservice
USER rhservice

ARG JAR_FILE=ssdc-rh-service*.jar
COPY target/$JAR_FILE /opt/ssdc-rh-service.jar
