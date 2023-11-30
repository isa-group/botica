FROM openjdk:11

WORKDIR /app/volume

COPY target/botica.jar /app/botica.jar

CMD ["java","-jar","/app/botica.jar"]