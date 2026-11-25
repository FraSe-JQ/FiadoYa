FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B package -DskipTests
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S fiadoya && adduser -S fiadoya -G fiadoya
WORKDIR /app
COPY --from=build /app/target/fiadoya-1.0.0.jar app.jar
USER fiadoya
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
