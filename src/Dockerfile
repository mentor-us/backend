FROM eclipse-temurin:17-jre-alpine
WORKDIR /workspace/
COPY ./target/backend-0.0.1.jar ./backend-0.0.1.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev","./backend-0.0.1.jar"]

EXPOSE 8080
EXPOSE 8085
