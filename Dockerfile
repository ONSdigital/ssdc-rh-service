FROM eclipse-temurin:17-jdk-alpine

ARG JAR_FILE=ssdc-rh-service*.jar
CMD ["/opt/java/openjdk/bin/java", "-jar", "/opt/ssdc-rh-service.jar"]
COPY healthcheck.sh /opt/healthcheck.sh
RUN addgroup --gid 1000 rhservice && \
    adduser --system --uid 1000 rhservice rhservice
USER rhservice

COPY target/$JAR_FILE /opt/ssdc-rh-service.jar
