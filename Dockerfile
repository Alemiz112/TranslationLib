FROM eclipse-temurin:17-jre-focal
# Create workspace
WORKDIR /usr/app
# Add build application jar
COPY service/build/libs/service-1.0-SNAPSHOT-all.jar ./service.jar

# Default to 8080
ENV APP_PORT 8080
EXPOSE 8080
# Start application
CMD ["java", "-jar", "service.jar"]