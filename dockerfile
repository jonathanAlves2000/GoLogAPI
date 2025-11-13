FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
