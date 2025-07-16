FROM openjdk:21-jdk

# Use lightweight and optimized base image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Set JVM options to reduce memory usage and boost performance
ENV JAVA_OPTS="-XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# Copy jar file from build context (fast if jar is cached)
ARG JAR_FILE=target/task-management-*.jar
COPY ${JAR_FILE} task-management.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar task-management.jar"]