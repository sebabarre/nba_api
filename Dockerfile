FROM openjdk:12.0.1-jdk

COPY target/nba_api-1.0-SNAPSHOT-jar-with-dependencies.jar nba_api.jar
EXPOSE 7777
ENTRYPOINT java -jar nba_api.jar