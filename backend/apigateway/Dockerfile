FROM maven:3.9.9-eclipse-temurin-17-alpine AS dependency

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

FROM dependency AS build

WORKDIR /app

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine AS runtime

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]