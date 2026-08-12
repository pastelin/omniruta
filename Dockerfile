# ============================================================
# ETAPA 1 - Compilación
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Primero copiamos pom para aprovechar cache de dependencias
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copiamos código
COPY src ./src

# Generamos el JAR
RUN mvn clean package -DskipTests


# ============================================================
# ETAPA 2 - Ejecución
# ============================================================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]