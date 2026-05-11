# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Mecânica API — a shop management system backend built as a FIAP postgraduate Tech Challenge (Group 14SOAT). Java 21 + Spring Boot 3.3.4, Hexagonal Architecture + DDD, PostgreSQL.

## Common Commands

```bash
# Start full environment (Docker)
make dev              # Build + start all services (recommended)
make rebuild          # Rebuild app only, keep data
make down             # Stop services
make clean-docker     # Full reset (removes volumes)
make logs             # Follow app logs

# Development (requires ./mvnw — generate with: ./mvn-docker.sh -N org.apache.maven.plugins:maven-wrapper-plugin:3.2.0:wrapper)
make build            # Compile only
make test             # Unit tests
make integration-test # Integration tests (TestContainers + real DB)
make coverage         # JaCoCo report → target/site/jacoco/index.html
make format           # Auto-format (Spotless/Google Java Format)
make lint             # Static analysis: Checkstyle + PMD + SpotBugs
make quality-fix      # Auto-fix: formatting + imports

# Run a single test class
./mvnw test -Dtest=AuthControllerTest

# Test reports
make allure-serve     # Interactive Allure report (hot-reload browser)

# Run locally (no Docker)
make run              # ./mvnw spring-boot:run (requires dev profile + local DB)
```

## Architecture

### Layer Structure
```
com/fiap/mecanica/
├── domain/           # Pure domain — entities, VOs, ports (interfaces), exceptions, events
├── application/      # Use cases — service interfaces + impls, DTOs, event listeners, email services
├── infra/            # Adapters — JPA entities, repository adapters, mappers, security config, seeding
├── presentation/     # REST layer — controllers, request/response DTOs, mappers, exception handler
└── infrastructure/   # External integrations (currently: Astrea integration placeholder)
```

### Hexagonal Pattern
- **Domain ports**: `domain/repository/` — pure Java interfaces (e.g., `OrdemServicoRepository`)
- **Adapters (outbound)**: `infra/adapter/Jpa*RepositoryAdapter.java` — implement domain ports using JPA
- **JPA repositories**: `infra/jpa/` — Spring Data interfaces used only by adapters
- **JPA entities**: `infra/entity/` — persistence-only POJOs, mapped to/from domain models via `infra/mapper/`
- **Domain models** (`domain/model/`) are plain Java objects — no JPA annotations, no Spring dependencies
- **Application services**: interfaces in `application/service/`, implementations in `application/service/impl/`
- **OpenAPI contracts**: `presentation/api/*ControllerApi.java` interfaces — controllers implement these

### OrdemServico State Machine
`RECEBIDA` → `EM_DIAGNOSTICO` → `AGUARDANDO_APROVACAO` → `APROVADA` → `EM_EXECUCAO` → `FINALIZADA` → `ENTREGUE`
Any state can transition to `CANCELADA`. Invalid transitions throw `TransicaoStatusInvalidaException`.

### Domain Events (Spring `ApplicationEvent`)
Published from application services, handled by `@TransactionalEventListener` + `@Async` listeners:
- `OsCriadaEvent`, `OrdemServicoAguardandoAprovacaoEvent`, `OrcamentoGeradoEvent`, `OrcamentoAprovadoEvent`, `OrcamentoReprovadoEvent`, `OsFinalizadaEvent`, `OrdemServicoCanceladaEvent`

### Exception Hierarchy
All domain exceptions extend either `DomainRuleException` or `BusinessException` (both implement `MecanicaError` with a `code` field like `"OS-422-01"`). `GlobalExceptionHandler` maps these to HTTP responses — `DomainRuleException` → 422, `ResourceNotFoundException` → 404, `DuplicateDocumento*` → 409.

### Security
JWT via JJWT 0.12.5. `JwtAuthenticationFilter` validates tokens. Role-based access via `@PreAuthorize`. `UserContext` provides the authenticated user to controllers. `OsSecurity` handles OS-specific authorization. Rate limiting via Bucket4j (10 req/min by default).

### Key Conventions
- **Two mapper layers**: `infra/mapper/` (domain model ↔ JPA entity) and `presentation/mapper/` (domain model ↔ request/response DTO)
- **Seeding**: `infra/seeding/` populates dev data on startup (controlled by `seeding.enabled` in `application.yml`)
- **Profiles**: `dev` (PostgreSQL at `:5433`, MailHog at `:1025`); tests use H2 in-memory
- **Migrations**: Flyway, 15 versioned SQL files in `src/main/resources/db/migration/`
- **Pagination**: `Pageable` default 20, max 100 items

## Docker Services

| Service | Port | Description |
|---------|------|-------------|
| `app` | 8080 | Main API + Swagger at `/swagger-ui.html` |
| `postgres` | 5433 | PostgreSQL (user: `mecanica_user`, pass: `mecanica_pass`, db: `mecanica`) |
| `adminer` | 8081 | DB browser (server: `postgres`) |
| `mailhog` | 8025 (web) / 1025 (SMTP) | Email catcher — no real emails sent |
| `sonarqube` | 9000 | Code quality dashboard |

## Seeded Test Users

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@mecanica.com` | `123456` |
| Mecânico | `mecanico@mecanica.com` | `123456` |
| Atendente | `atendente@mecanica.com` | `123456` |
| Cliente | `cliente@mecanica.com` | `123456` |

## Testing Patterns

- **Unit tests** (`src/test/java/.../domain/`, `.../application/`): JUnit 5 + Mockito, no Spring context
- **Integration tests** (`src/test/java/.../integration/`): `@SpringBootTest` + TestContainers (real PostgreSQL), uses `integration-tests` Maven profile
- **E2E tests** (`src/test/java/.../e2e/`): RestAssured against running container
- **Allure annotations** (`@Epic`, `@Feature`, `@Story`) are used in test classes for rich reporting

## Code Quality Gates

Pre-commit hook (installed via `make install-hooks`) runs Spotless auto-format. CI enforces:
- Checkstyle (Google Java Style)
- PMD (cyclomatic complexity, dead code)
- SpotBugs (null dereference, resource leaks)
- JaCoCo: 90% overall, 80% domain coverage minimum

## Architecture Decision Records

28 ADRs in `docs/ADRs/` document key design choices. Notable ones:
- ADR-003: Task-based OS status API (actions as sub-resources under `/ordens-servico/{id}/acoes`)
- ADR-010: Hybrid authorization strategy (JWT roles + object-level security)
- ADR-019: Decoupling Orcamento from Estoque (stock deducted on OS approval, not budget approval)
- ADR-022: Inverted OS/Orcamento approval flow
