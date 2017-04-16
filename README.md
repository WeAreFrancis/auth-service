# Auth Service
## Prerequisites
- Maven

## Build
`mvn package`

## Run
```
export AUTH_POSTGRESQL_USER=user
export AUTH_POSTGRESQL_PASSWORD=password

docker-compose up -d
mvn spring-boot:run
```