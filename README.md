# ledgerpay

A ledger-based payment processing platform built with Java 21, Spring Boot, PostgreSQL, Kafka and Redis. Personal project using mock data only.

**Planned services:** API gateway, payment, ledger (append-only double-entry), account (balance read model), fraud, reconciliation.

**Current status:** Phase 2 complete (ledger service).

## Prerequisites
- JDK 21
- Docker Desktop

## Run locally
```bash
# 1. Start PostgreSQL, Kafka, Redis and Kafka UI
docker compose up -d

# 2. Build all modules (tests need PostgreSQL from step 1)
./mvnw clean install

# 3. Start a service (use a separate terminal per service)
./mvnw -pl ledger-service spring-boot:run
./mvnw -pl payment-service spring-boot:run
./mvnw -pl account-service spring-boot:run
```

Health checks:

| Service | URL |
|---|---|
| payment | http://localhost:8081/actuator/health |
| ledger | http://localhost:8082/actuator/health |
| account | http://localhost:8083/actuator/health |

Kafka UI: http://localhost:8090

If the databases do not exist (the init script only runs on a fresh volume), run `docker compose down -v` and `docker compose up -d` again. This deletes local data.

Build without tests: `./mvnw clean install -DskipTests`

## Architecture decisions
See [docs/adr](docs/adr).
