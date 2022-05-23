FROM openjdk:11-jdk-slim
COPY "./target/CustomerService-0.0.1-SNAPSHOT.jar" "app.jar"
EXPOSE 8081 
ENTRYPOINT ["java","-jar","app.jar"]