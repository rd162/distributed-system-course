FROM openjdk:8u212-jdk-slim
ADD target/rpc-service-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 50051 50051
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
