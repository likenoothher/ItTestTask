FROM openjdk:16-alpine3.13
EXPOSE 8080
COPY build/libs/springJwt-1.0.jar springJwtApp.jar
ENTRYPOINT ["java","-jar","springJwtApp.jar"]
