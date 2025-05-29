FROM openjdk:23
WORKDIR /app
COPY target/ChatServer.jar app.jar
COPY truststore/ truststore/
CMD ["java", "-jar", "app.jar"]