# ==========================================
# STAGE 1: Compilação do Backend
# ==========================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Imagem Final de Execução
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/iluminacao-minas-backend-1.0.0.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
