# Optimize JAR
FROM eclipse-temurin:21-jre AS builder
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Build final image
FROM eclipse-temurin:21-jdk
EXPOSE 8081

# Copy layered JAR
WORKDIR /app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Spring
ENV SPRING_PROFILES_ACTIVE=prod

# Start the main class with an explicit classpath instead of the Spring Boot JarLauncher.
# Student code is compiled at runtime with javac, which only sees the JVM's real classpath
# (java.class.path). With JarLauncher, BOOT-INF/lib (incl. the H2 driver) is not on it.
ENTRYPOINT ["java", "-Xmx6g", "-cp", "/app/BOOT-INF/classes:/app/BOOT-INF/lib/*", "at.jku.dke.task_app.jdbc.TaskAppApplication"]
