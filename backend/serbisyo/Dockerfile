FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy necessary files from the serbisyo directory
COPY backend/serbisyo/mvnw ./mvnw
RUN chmod +x ./mvnw

COPY backend/serbisyo/.mvn/ ./.mvn/
COPY backend/serbisyo/pom.xml ./

# Install dependencies
RUN ./mvnw dependency:go-offline

# Copy the src directory
COPY backend/serbisyo/src/ ./src/

# Build the application
RUN ./mvnw package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/serbisyo-0.0.1-SNAPSHOT.jar"]