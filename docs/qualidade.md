# Qualidade e Testes — Mecânica API

## Pirâmide de Testes

```
           ▲   E2E / Carga (RestAssured, k6)
          ▲▲▲  Integração (TestContainers + PostgreSQL real)
        ▲▲▲▲▲▲ Unitários (JUnit 5 + Mockito)
```

**Total:** 1052+ testes — `./mvnw clean test` → BUILD SUCCESS

### Unitários (`src/test/.../domain/`, `.../application/`)
- Sem contexto Spring — puro JUnit 5 + Mockito
- Cobrem regras de negócio, entidades, VOs e casos de uso isolados
- `make test`

### Integração (`src/test/.../integration/`)
- `@SpringBootTest` + TestContainers (PostgreSQL real)
- Cobre repositórios JPA, controllers e fluxos E2E de componentes
- `make integration-test`

### Carga (`load-tests/`)
- k6 — fluxos críticos sob concorrência (auth + OS operations)
- `make load-test`

---

## Cobertura (JaCoCo)

| Escopo | Meta |
|--------|------|
| Geral | ≥ 90% |
| Domínio | ≥ 80% |

```bash
make coverage   # relatório em target/site/jacoco/index.html
```

---

## Análise Estática

| Ferramenta | Propósito |
|------------|-----------|
| **Checkstyle** | Google Java Style — formatação e convenções |
| **PMD** | Más práticas, código morto, complexidade ciclomática |
| **SpotBugs** | NullPointer, resource leaks, bugs potenciais |
| **Spotless** | Auto-formatação (Google Java Format) — pre-commit hook |
| **SonarQube** | Dashboard consolidado: code smells + vulnerabilidades |

```bash
make lint           # Checkstyle + PMD + SpotBugs
make format         # Spotless auto-fix
make install-hooks  # registra pre-commit hook (Spotless automático)
```

---

## Segurança

- **Trivy** — scan de vulnerabilidades em imagens Docker (pipeline CI/CD)
- **OWASP Dependency Check** — CVEs em dependências Maven
- **JWT secret** sem valor default em `application.yml` base — obrigatório via env var em produção
- **Rate limiting** — Bucket4j (10 req/min por padrão, configurável)

---

## Relatórios

```bash
make allure-serve   # Allure interativo — histórico de execuções, agrupamento por Epic/Feature/Story
make coverage       # JaCoCo HTML em target/site/jacoco/index.html
```

Anotações Allure nos testes: `@Epic`, `@Feature`, `@Story` — rastreabilidade por funcionalidade.

---

## Decisões Relacionadas

| ADR | Decisão |
|-----|---------|
| [ADR-018](./ADRs/ADR-018-estrategia-testes-e2e.md) | Estratégia de testes E2E |
| [ADR-027](./ADRs/ADR-027-estrategia-testes-integrados.md) | Pirâmide de testes integrados |
| [ADR-029](./ADRs/ADR-029-estrategia-testes-carga-interface.md) | Load testing com k6 |
