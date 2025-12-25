# Étape 1 : Build avec Maven
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale avec JDK
FROM eclipse-temurin:21-jdk
LABEL authors="sa"
WORKDIR /app

# Copier le jar depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port Spring Boot
EXPOSE 9191

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
