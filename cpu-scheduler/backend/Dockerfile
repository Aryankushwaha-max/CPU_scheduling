FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/cpu-scheduler-1.0.0.jar app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -XX:MaxRAMPercentage=70 -Dserver.port=${PORT:-8080} -jar app.jar"]
