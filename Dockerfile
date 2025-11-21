# Multi-stage build for Spring Boot (Java 21)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Maven wrapper and config first (better layer caching)

COPY mvnw mvnw
RUN chmod +x mvnw
COPY mvnw.cmd mvnw.cmd
COPY .mvn .mvn
COPY pom.xml pom.xml

# Diagnóstico: listar arquivos e mostrar início do mvnw
RUN ls -l /app
RUN head -20 /app/mvnw

# Pre-fetch dependencies
RUN ./mvnw -q -B -DskipTests dependency:go-offline

# Copy sources and build
COPY src src
RUN ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV TZ=America/Sao_Paulo \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    SERVER_PORT=8081 \
    SPRING_PROFILES_ACTIVE=fly

# Copy fat JAR from builder
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8081
USER 1001

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=$SERVER_PORT -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
