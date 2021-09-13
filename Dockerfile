FROM openjdk:16-alpine3.13
WORKDIR /src
EXPOSE 8080
ARG JAR_FILE=target/test.jar
COPY ${JAR_FILE} test.jar
ENTRYPOINT ["java","-jar","test.jar"]
