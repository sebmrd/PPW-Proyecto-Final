# Etapa de construcción
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
# Damos permisos al wrapper y construimos descartando los tests para agilizar
RUN chmod +x gradlew
RUN ./gradlew build -x test

# Etapa de ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# Rúbrica: Limitar la memoria de la JVM mediante JAVA_TOOL_OPTIONS
ENV JAVA_TOOL_OPTIONS="-Xmx512m -Xms256m"

# Puerto dinámico expuesto
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]