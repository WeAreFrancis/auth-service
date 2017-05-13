# Auth Service
## Prerequisites
- Docker
- Docker Compose
- Maven

## Run
```
export AUTH_API_URL="http://172.0.0.10:8080"
export AUTH_JWT_SECRET=my-secret
export AUTH_SMTP_HOST=hostname
export AUTH_SMTP_PORT=port
export AUTH_SMTP_USERNAME=username
export AUTH_SMTP_PASSWORD=password

mvn clean package
docker-compose up -d
```
