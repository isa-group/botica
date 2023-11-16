FROM openjdk:11

WORKDIR /app

COPY target/botica.jar /app/botica.jar
COPY rabbitmq/server-config.json /app/rabbitmq/

CMD ["java","-jar","/app/botica.jar"]