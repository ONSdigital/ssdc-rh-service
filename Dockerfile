FROM eclipse-temurin:17-jdk-alpine

CMD ["java", "-jar", "/opt/ssdc-rh-service.jar"]

RUN addgroup --gid 1000 rhservice && \
    adduser --system --uid 1000 rhservice rhservice
USER rhservice

COPY target/ssdc-rh-service*.jar /opt/ssdc-rh-service.jar
