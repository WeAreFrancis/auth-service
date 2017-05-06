# Auth Service
## Prerequisites
- Maven

## Build
`mvn package`

## Run
```
export AUTH_POSTGRES_USER=user
export AUTH_POSTGRES_PASSWORD=password

docker-compose up -d
mvn spring-boot:run
```