# Usa un'immagine di base con JDK 17 (o la versione necessaria)
FROM openjdk:17-jdk-slim

# Imposta la directory di lavoro
WORKDIR /app

# Copia il file JAR generato da Maven/Gradle
COPY target/ecommerce-project-0.0.1-SNAPSHOT.jar app.jar

# Esponi la porta dell'applicazione
EXPOSE 8080

# Avvia l'applicazione
CMD ["java", "-jar", "app.jar"]
