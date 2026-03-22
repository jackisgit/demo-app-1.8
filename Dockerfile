FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM docker.m.daocloud.io/library/eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/demo-app-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
