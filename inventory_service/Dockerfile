FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/inventory_service-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]