# Etapa 1: Construcción (Build)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY task-manager-api/pom.xml .
COPY task-manager-api/.mvn .mvn
COPY task-manager-api/mvnw .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY task-manager-api/src ./src

RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]