# Disturbance

## Leverantör

Sundsvalls kommun

## Beskrivning
Disturbance är en tjänst som hanterar aviseringar vid driftsavbrott.


## Tekniska detaljer

### Integrationer
Tjänsten integrerar mot:

* Messaging

### Starta tjänsten

|Miljövariabel|Beskrivning|
|---|---|
|**Databasinställningar**||
|`QUARKUS_DATASOURCE_DB_KIND`|Typ av databas|
|`QUARKUS_DATASOURCE_JDBC_URL`|JDBC-URL för anslutning till databas|
|`QUARKUS_DATASOURCE_USERNAME`|Användarnamn för anslutning till databas|
|`QUARKUS_DATASOURCE_PASSWORD`|Lösenord för anslutning till databas|
|`QUARKUS_HIBERNATE_ORM_DIALECT`|Databasdialekt|
|`QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION`|Anger om, och i sådana fall hur databasen ska hanteras vid uppstart. Giltiga värden: `none`, `update`, `validate`, `drop-and-create` (endast i test)|
|**Inställningar för tjänsten Messaging**|
|`API_MESSAGING_MP_REST_URL`| API-URL till tjänsten Messaging|
|`QUARKUS_OIDC_CLIENT_API_MESSAGING_AUTH_SERVER_URL`| URL för att hämta OAuth2-token för Messaging|
|`QUARKUS_OIDC_CLIENT_API_MESSAGING_TOKEN_PATH`| Path för token resursen (t.ex. /token)|
|`QUARKUS_OIDC_CLIENT_API_MESSAGING_CLIENT_ID`| OAuth2-klient-id för Messaging |
|`QUARKUS_OIDC_CLIENT_API_MESSAGING_CREDENTIALS_SECRET`| OAuth2-klient-nyckel för Messaging |


### Paketera och starta tjänsten
Applikationen kan paketeras genom:

```
./mvnw package
```
Kommandot skapar filen `quarkus-run.jar` i katalogen `target/quarkus-app`. Tjänsten kan nu köras genom kommandot `java -jar target/quarkus-app/quarkus-run.jar`.

### Bygga och starta med Docker
För att bygga en Docker-image:

```
docker build -f src/main/docker/Dockerfile.jvm -t api.sundsvall.se/ms-disturbance:latest .
```

För att starta samma Docker-image i en container:

```
docker run -i --rm -p 8080:8080 api.sundsvall.se/ms-disturbance
```

## 
Copyright (c) 2021 Sundsvalls kommun