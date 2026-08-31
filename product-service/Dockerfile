# Etapa 1> Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

#Etapa 2> Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar product.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","product.jar"]

