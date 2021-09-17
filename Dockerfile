FROM openjdk:16-alpine3.13
EXPOSE ${SPRING_JWT_APP_PORT}
COPY build/libs/springJwt-1.0.jar springJwtApp.jar
ENTRYPOINT ["java","-jar","springJwtApp.jar"]
