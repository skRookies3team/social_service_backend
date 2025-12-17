FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY build/libs/*.jar social-service.jar
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75","-jar","social-service.jar"]
