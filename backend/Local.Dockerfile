FROM eclipse-temurin:17-jdk AS BUILD
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod 777 mvnw
COPY pom.xml .
COPY src/ ./src/
RUN ./mvnw --no-transfer-progress clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=BUILD /build/target/backend-*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
