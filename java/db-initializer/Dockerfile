FROM maven:3.9.5 AS build

WORKDIR /java

COPY . .

ARG ARTIFACT_ID
RUN mvn clean install -pl ${ARTIFACT_ID} -am

FROM eclipse-temurin:21-jdk-jammy

WORKDIR /java
ARG ARTIFACT_ID
COPY --from=build /java/${ARTIFACT_ID}/target/${ARTIFACT_ID}*.jar app.jar
RUN chmod +x app.jar
#COPY ./db-initializer/src/main/resources/test-data/2024-05-23-11-26-05-ts-error-F8-generated-with-ts-travel-service.tar.gz /test-data/
#RUN cd /test-data && tar -xvf /test-data/2024-05-23-11-26-05-ts-error-F8-generated-with-ts-travel-service.tar.gz && rm 2024-05-23-11-26-05-ts-error-F8-generated-with-ts-travel-service.tar.gz

ENTRYPOINT ["java", "-jar", "app.jar"]
