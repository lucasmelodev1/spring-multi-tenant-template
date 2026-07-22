# AGENTS.md

## Build & Run

```bash
# Build (requires Docker running for Testcontainers)
./mvnw clean install

# Run tests only
./mvnw test

# Full verification (unit + integration tests via Testcontainers)
./mvnw verify
```

Docker must be running for `./mvnw verify` — tests spin up PostgreSQL, Kafka, and Grafana via Testcontainers.

## Database

- **Flyway** manages migrations: `src/main/resources/db/migrations/`
- `out-of-order: true` — migration filenames use `V<timestamp>__<description>.sql`
- JPA `ddl-auto: validate` — schema changes require a migration, never `update`

## Code Patterns

- **UUIDv7 primary keys**: Use `@UuidV7Id` annotation on `@Id` fields (custom Hibernate generator in `com.example.demo.utils`)
- **Soft delete**: `@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")` — never hard-delete entities
- **DTOs**: Java records in `com.example.demo.user.dto`
- **Auth tokens**: 6-digit numeric codes, stored as SHA-256 hashes via `TokenUtils.hash()` — never store plaintext tokens

## Configuration

- `application.yaml` (not `.properties`)
- Datasource from env vars: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` with defaults in `application.yaml`
- `.env.example` has Postgres defaults; copy to `.env` for local dev
