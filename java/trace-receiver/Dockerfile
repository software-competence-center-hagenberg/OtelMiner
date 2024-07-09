FROM maven:3.9.5 AS build

WORKDIR /java

COPY . .

ARG ARTIFACT_ID
RUN mvn clean install -pl ${ARTIFACT_ID} -am

FROM eclipse-temurin:21-jdk-jammy

ARG JAR_FILE
COPY --from=build ${JAR_FILE} app.jar
RUN chmod +x app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]