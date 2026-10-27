### Run PostgreSQL for the MVP

Start Docker Desktop, configure POSTGRES_DB, POSTGRES_USER and POSTGRES_PASSWORD
in the local .env file, then run:

```powershell
docker compose up -d postgres
mvn spring-boot:run
```

Compose runs PostgreSQL 17 only, bound to localhost:5433, with persistent storage.
The Spring application runs on the host and applies Flyway migrations on startup.
Set SENTRA_ADMIN_EMAIL and SENTRA_ADMIN_PASSWORD for first-administrator creation.

Open http://localhost:8080/swagger-ui/index.html and use Authorize.
See [README.md](README.md) for environment configuration, tests and API examples.

The existing Dockerfile is optional and was not part of MVP verification.
Cloud deployment and application containerization are outside this MVP.
