FROM eclipse-temurin:17-jdk-alpine

RUN apk --no-cache add curl

COPY ./target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
