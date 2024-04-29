FROM openjdk:17-slim AS BUILD
WORKDIR /build
COPY mvnw .
COPY .mvn/ ./.mvn/
COPY pom.xml .
COPY src/ ./src/
RUN ./mvnw package -DskipTests  # todo: починить тесты

FROM openjdk:17-slim
WORKDIR /app
COPY --from=BUILD /build/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
