FROM openjdk:11-slim-buster AS builder
COPY . /home/src
WORKDIR /home/src
RUN /home/src/gradlew bootJar

FROM openjdk:11-slim-buster
EXPOSE 8080
COPY --from=builder /home/src/build/libs/*.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
