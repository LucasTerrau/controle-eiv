# Etapa 1: compila o projeto
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B package -DskipTests

# Etapa 2: imagem final, só com o Java de execução e o .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/controle-eiv-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
