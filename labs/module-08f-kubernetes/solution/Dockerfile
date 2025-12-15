FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=k8s-demo/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
