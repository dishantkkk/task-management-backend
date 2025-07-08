FROM openjdk:21-jdk
VOLUME /tmp
ARG JAR_FILE=target/task-management-*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
