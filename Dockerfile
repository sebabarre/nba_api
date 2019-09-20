FROM openjdk:12.0.1-jdk

COPY target/nba_api*dependencies.jar nba_api.jar
EXPOSE 7777
CMD java -jar nba_api.jar