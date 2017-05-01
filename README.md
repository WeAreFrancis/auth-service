# Auth Service
## Prerequisites
- Docker
- Docker Compose
- Maven

## Run
```
export AUTH_API_URL="http://172.0.0.20:8080"

export AUTH_POSTGRESQL_USER=user
export AUTH_POSTGRESQL_PASSWORD=password

export AUTH_SMTP_HOST=hostname
export AUTH_SMTP_PORT=port
export AUTH_SMTP_USERNAME=username
export AUTH_SMTP_PASSWORD=password

mvn clean package
docker-compose up -d
```