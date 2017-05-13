# Auth Service
## Prerequisites
- Docker
- Docker Compose
- Maven

## Run
```
docker-compose up -d
mvn spring-boot:run  \
        -DAPI_URL=http://localhost:8080 \
        -DJWT_SECRET=my-jwt-secret \
        -DDB_HOST=172.0.0.10 \
        -DPOSTGRES_USER=auth \
        -DPOSTGRES_PASSWORD=auth \
        -DSMTP_USER=my-email-address@my-domain.com \
        -DDB_NAME=auth \
        -DSMTP_HOST=smtp-host \
        -DSMTP_PORT=smtp-port \
        -DSMTP_PASSWORD=my-password
```
