FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/myapp.jar
COPY ${JAR_FILE} first_app.jar
ENTRYPOINT ["java","-jar","/first_app.jar"]