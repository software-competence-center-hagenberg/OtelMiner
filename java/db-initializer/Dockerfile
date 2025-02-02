FROM maven:3.9.5 AS build

WORKDIR /java

COPY ./.. .
COPY ./src/main/resources/test-data ./test-data

ARG ARTIFACT_ID
RUN mvn clean install -pl ${ARTIFACT_ID} -am

FROM eclipse-temurin:21-jdk-jammy

ARG ARTIFACT_ID
COPY --from=build /java/${ARTIFACT_ID}/target/${ARTIFACT_ID}*.jar app.jar
RUN chmod +x app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
