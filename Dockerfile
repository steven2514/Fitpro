# Imagen para desplegar FitPro (Render, Railway o cualquier servicio con Docker).

# 1) Compilar: las dependencias se descargan en una capa aparte para reutilizarla entre despliegues
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# 2) Ejecutar: sólo el JRE y el .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/fitpro-1.0.0.jar app.jar
# Los planes pequeños tienen 512 MB: limitar la memoria evita que el servicio mate la app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
ENV SPRING_PROFILES_ACTIVE=prod
# El servicio indica el puerto con la variable PORT (la app ya la lee)
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
