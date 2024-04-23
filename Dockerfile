FROM openjdk:22-jdk
WORKDIR /app
COPY /target/*.jar /app/
COPY /config /app/config