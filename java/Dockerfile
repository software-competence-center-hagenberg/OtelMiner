FROM maven:3.9.5 AS build

WORKDIR /java

COPY . .

ARG ARTIFACT_ID
RUN mvn clean install -pl ${ARTIFACT_ID} -am
RUN mv /java/${ARTIFACT_ID}/target/${ARTIFACT_ID}*.jar /app.jar
RUN rm -r /java

FROM eclipse-temurin:21-jdk-jammy

ARG ARTIFACT_ID
COPY --from=build /app.jar app.jar
RUN chmod +x app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]