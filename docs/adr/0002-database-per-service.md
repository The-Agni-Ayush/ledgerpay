# 2. Database per service

**Status:** Accepted

## Context
Ledger, payment and account data have different owners and access patterns. A shared database would make all services depend on one schema and turn the system into a hidden monolith.

## Decision
Each service owns its own database (`ledger_db`, `payment_db`, `account_db`, `recon_db`) and is the only service allowed to access it. Other services get data through APIs or Kafka events. Locally, one PostgreSQL container hosts all databases for convenience; in production they would be separate instances. Schema changes are managed with Flyway migrations.

## Consequences
- Services can change their schema independently.
- No cross-service SQL joins or ACID transactions; consistency across services uses the Saga and Outbox patterns.
- More moving parts to operate than a single shared database.
