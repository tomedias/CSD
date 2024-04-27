FROM openjdk:21-jdk
WORKDIR /app
COPY /target/*.jar /app/
COPY /config /app/config
COPY /tls /app/tls