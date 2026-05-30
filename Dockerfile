FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY . .
RUN ./gradlew build -x test
EXPOSE 8082
CMD ["java", "-jar", "build/libs/turboDetectAI-0.0.1-SNAPSHOT.jar"]