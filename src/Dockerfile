FROM eclipse-temurin:21-jre-alpine AS builder
COPY ./target/app.jar ./app.jar
RUN java -Djarmode=layertools -jar ./app.jar extract

FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /dependencies/ ./
COPY --from=builder /spring-boot-loader/ ./
COPY --from=builder /snapshot-dependencies/ ./
COPY --from=builder /application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]

EXPOSE 8080
EXPOSE 8085