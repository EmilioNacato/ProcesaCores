FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY target/banquito-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]