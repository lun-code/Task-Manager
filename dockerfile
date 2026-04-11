# Etapa 1: Construcción (Build)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiamos primero el contenido de la subcarpeta del backend
COPY task-manager-api/ .

# Damos permisos de ejecución al wrapper de Maven y compilamos
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el JAR generado desde la etapa anterior
# Maven por defecto lo deja en /app/target/
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Usamos un array para el ENTRYPOINT (mejor práctica)
ENTRYPOINT ["java", "-jar", "app.jar"]