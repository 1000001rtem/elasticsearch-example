FROM openjdk:11-jdk-slim

EXPOSE 8080

COPY ./build/libs/*.jar .

CMD java -Xmx1024m -jar  *.jar
