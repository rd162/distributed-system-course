FROM openjdk:8u212-jdk-slim
ADD target/web-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080 60000
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
