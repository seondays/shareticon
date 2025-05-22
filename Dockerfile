FROM openjdk:17-jdk-slim
WORKDIR /app

ARG JAR_FILE=./build/libs/shareticon-0.0.1-SNAPSHOT.jar

COPY $JAR_FILE /app/shareticon.jar

EXPOSE 8080

ENV TZ=Asia/Seoul

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "shareticon.jar"]
