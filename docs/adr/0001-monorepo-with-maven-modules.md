# 1. Monorepo with Maven modules

**Status:** Accepted

## Context
The platform is made of several Spring Boot services that share a Java version, a Spring Boot version and one CI pipeline. It is built and maintained by a single developer.

## Decision
Keep all services in one Git repository as modules of a parent Maven POM (`ledgerpay`). The parent inherits from `spring-boot-starter-parent` and pins the Java and Spring Boot versions once. Each service inherits from the parent.

## Consequences
- One clone, one build, one README: easy to review and run.
- Versions cannot drift between services.
- Services are still deployed independently (one Docker image each).
- If the team grew, services could be split into separate repositories.
