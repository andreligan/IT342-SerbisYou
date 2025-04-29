FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Make sure that the script is executable
COPY mvnw ./mvnw
RUN chmod +x ./mvnw

COPY .mvn/ .mvn/
COPY pom.xml ./

# Install dependencies
RUN ./mvnw dependency:go-offline

# Copy the src directory
COPY src/ ./src/

# Build the application
RUN ./mvnw package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/*.jar"] 