FROM openjdk:16-alpine3.13
EXPOSE 8080
COPY target/test.jar test.jar
ENTRYPOINT ["java","-jar","test.jar"]
