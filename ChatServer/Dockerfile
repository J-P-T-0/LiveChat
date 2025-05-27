FROM maven:3.9-amazoncorretto-23 AS builder
#modificacion para una build mas ligera
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Etapa final
FROM openjdk:23-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY truststore /app/truststore

# Configuración de seguridad
RUN addgroup --system javauser && adduser --system --ingroup javauser javauser
USER javauser

CMD ["java", "-jar", "app.jar"]