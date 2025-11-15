FROM openjdk:26-ea-17-slim
#ARG JAR_FILE=target/myapp.jar
COPY /target/FirstAzureCloudApp-0.0.1-SNAPSHOT.jar first_localDB_app.jar
EXPOSE 8989
ENTRYPOINT ["java","-jar","/first_localDB_app.jar"]