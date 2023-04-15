# NBA_API

## But

Offrir en Json les informations nécessaires à l'affichage du ruinartChallenge
* Le classement nba
* Les pronos des joueurs

## Faire tourner en local

```
mvn clean install
java -jar target/nba_api-<VERSION>-jar-with-dependencies.jar
```

tester http://localhost:7777/standings et http://localhost:7777/pronos

## Builder en local

```
docker build . -t nba_api_<VERSION>
```

