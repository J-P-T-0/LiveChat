FROM openjdk:23
WORKDIR /app
COPY target/ChatServer.jar app.jar
COPY truststore/ truststore/
CMD ["java", "-jar", "app.jar"]
ENV TRUSTSTORE_PASS: cb.pp3_UPSLP
ENV DB_URL: jdbc:mysql://127.0.0.1:1234/appdb
ENV DB_USER: root
ENV DB_PASS: