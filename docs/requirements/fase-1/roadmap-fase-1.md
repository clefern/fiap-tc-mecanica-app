# Roadmap Fase 1 — Mecânica API
## Tech Challenge FIAP · Grupo 14SOAT

> **Status**: ✅ Completamente implementado e entregue
> **Última atualização**: 2026-03-19

---

## Objetivo da Fase 1

Construir o **núcleo de negócio** do sistema de gestão de oficinas mecânicas:
API REST completa com DDD, Arquitetura Hexagonal, autenticação JWT, testes com
cobertura ≥ 90% e documentação Swagger.

---

## Requisitos Oficiais vs Implementação

| # | Requisito Oficial | Status | Evidência |
|---|------------------|--------|-----------|
| 1 | Gestão de Ordens de Serviço com máquina de estados | ✅ | `domain/model/OrdemServico.java` + `StatusOS` enum |
| 2 | Orçamentos com aprovação do cliente | ✅ | `domain/model/Orcamento.java` + fluxo invertido (ADR-022) |
| 3 | Controle de estoque de peças e insumos | ✅ | `domain/model/ItemComercial.java` (Peca + Insumo) + reserva na aprovação |
| 4 | Cadastro de clientes e veículos | ✅ | `domain/model/Cliente.java` + `Veiculo.java` |
| 5 | Cadastro de mecânicos e atendentes | ✅ | `domain/model/Mecanico.java` + `Atendente.java` |
| 6 | Catálogo de serviços | ✅ | `domain/model/Servico.java` + CRUD completo |
| 7 | Autenticação e autorização por perfil | ✅ | JWT JJWT 0.12.5 + roles + `@PreAuthorize` |
| 8 | Relatórios operacionais | ✅ | Tempo médio de serviços + PDF de OS finalizada |
| 9 | Testes com cobertura ≥ 90% (global) / 80% (domínio) | ✅ | JaCoCo gate ativo no CI |
| 10 | Documentação Swagger / OpenAPI | ✅ | 15 interfaces `*Api` + `/swagger-ui.html` |
| 11 | Collection Insomnia com fluxo E2E | ✅ | `docs/api/Insomnia_export.yaml` |

---

## Entidades de Domínio Implementadas

### Aggregate Root
| Entidade | Localização | Responsabilidade |
|----------|-------------|-----------------|
| `OrdemServico` | `domain/model/OrdemServico.java` | Orquestra todo o ciclo de atendimento — máquina de estados, itens, orçamento |

### Entidades
| Entidade | Localização | Responsabilidade |
|----------|-------------|-----------------|
| `Orcamento` | `domain/model/Orcamento.java` | Vinculado à OS; estados: GERADO → APROVADO \| REJEITADO \| CANCELADO |
| `Veiculo` | `domain/model/Veiculo.java` | Veículo do cliente — placa, modelo, marca, ano |
| `ItemComercial` (abstract) | `domain/model/ItemComercial.java` | Base para Peca e Insumo |
| `Peca` | `domain/model/Peca.java` | Peça com controle de estoque |
| `Insumo` | `domain/model/Insumo.java` | Insumo com controle de estoque |
| `Servico` | `domain/model/Servico.java` | Serviço de mão de obra do catálogo |

### Usuários (herança)
| Entidade | Localização | Role |
|----------|-------------|------|
| `User` (abstract) | `domain/model/User.java` | Base com autenticação |
| `Cliente` | `domain/model/Cliente.java` | ROLE_CLIENTE |
| `Atendente` | `domain/model/Atendente.java` | ROLE_ATENDENTE |
| `Mecanico` | `domain/model/Mecanico.java` | ROLE_MECANICO |
| `Admin` | `domain/model/Admin.java` | ROLE_ADMIN |

### Value Objects
| VO | Validação |
|----|-----------|
| `CPF` | Algoritmo módulo 11 |
| `CNPJ` | Algoritmo módulo 11 |
| `Email` | RFC 5322 |
| `PlacaVeiculo` | Padrão Mercosul + antigo |
| `Endereco` | CEP + logradouro + cidade + estado |
| `TelefoneBr` | DDD + número (fixo/celular) |

---

## Máquina de Estados — OrdemServico

```
RECEBIDA
   │
   ▼
EM_DIAGNOSTICO
   │
   ▼
AGUARDANDO_APROVACAO  ◄─── Orçamento gerado → email com link HMAC
   │
   ▼ (cliente aprova via link ou JWT)
APROVADA  ◄─── Estoque baixado automaticamente aqui (ADR-019)
   │
   ▼
EM_EXECUCAO
   │
   ▼
FINALIZADA  ◄─── PDF de entrega gerado automaticamente
   │
   ▼
ENTREGUE

CANCELADA  ◄─── qualquer estado (estorno de estoque automático)
```

Transições inválidas lançam `TransicaoStatusInvalidaException` (HTTP 422).

---

## Domain Events (Spring ApplicationEvent)

| Evento | Publisher | Handler | Efeito |
|--------|-----------|---------|--------|
| `OsCriadaEvent` | `OrdemServicoService` | `OsCreatedListener` | Log / notificação |
| `OrcamentoGeradoEvent` | `OrcamentoService` | `OrcamentoEmailListener` | Envia email ao cliente com link HMAC |
| `OrdemServicoAguardandoAprovacaoEvent` | `OrdemServicoService` | `OsAguardandoListener` | Notificação |
| `OrcamentoAprovadoEvent` | `OrcamentoService` | `EstoqueListener` | Baixa estoque das peças |
| `OrcamentoReprovadoEvent` | `OrcamentoService` | `OsListener` | Cancela reservas |
| `OsFinalizadaEvent` | `OrdemServicoService` | `PdfListener` | Gera PDF de entrega |
| `OrdemServicoCanceladaEvent` | `OrdemServicoService` | `EstoqueListener` | Estorna estoque |

Todos os handlers usam `@TransactionalEventListener` + `@Async`.

---

## API REST — Controllers e Endpoints

| Controller | Base Path | Principal Operação |
|------------|-----------|-------------------|
| `AuthController` | `/auth` | Login JWT (público) |
| `ClienteController` | `/api/clientes` | CRUD de clientes |
| `AtendenteController` | `/api/atendentes` | CRUD de atendentes |
| `MecanicoController` | `/api/mecanicos` | CRUD de mecânicos |
| `VeiculoController` | `/api/veiculos` | CRUD de veículos |
| `OrdemServicoController` | `/api/ordens-servico` | Listagem, busca, abertura |
| `OrdemServicoAcaoController` | `/api/ordens-servico/{id}/acoes` | Transições de estado (task-based API, ADR-003) |
| `OrdemServicoPrioridadeController` | `/api/ordens-servico/{id}/prioridade` | Gestão de prioridade |
| `OrcamentoController` | `/api/orcamentos` | Geração e aprovação de orçamentos |
| `PecaController` | `/api/pecas` | Catálogo de peças |
| `InsumoController` | `/api/insumos` | Catálogo de insumos |
| `ServicoController` | `/api/servicos` | Catálogo de serviços |
| `EstoqueController` | `/api/estoque` | Consulta e ajuste de estoque |
| `RelatorioController` | `/api/relatorios` | Tempo médio, PDF de OS |

---

## Segurança Implementada

| Camada | Implementação |
|--------|---------------|
| Autenticação | JWT JJWT 0.12.5 — `JwtAuthenticationFilter` |
| Autorização por role | `@PreAuthorize("hasRole('...')")` |
| Autorização por objeto | `@OsSecurity.canView/Edit(authentication, osId)` — ADR-010 |
| Rate limiting | Bucket4j — 10 req/min por IP (configurável) |
| Endpoints públicos | `/auth/**`, `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**` |

---

## Qualidade e Testes

### Cobertura
| Escopo | Meta | Status |
|--------|------|--------|
| Global | ≥ 90% | ✅ Gate ativo no CI |
| Domínio | ≥ 80% | ✅ Gate ativo no CI |

### Ferramentas
| Ferramenta | Propósito | Comando |
|------------|-----------|---------|
| JUnit 5 + Mockito | Testes unitários (sem Spring) | `make test` |
| TestContainers | Testes integrados (PostgreSQL real) | `make integration-test` |
| RestAssured | Testes E2E | `make integration-test` |
| JaCoCo | Cobertura de código | `make coverage` |
| Allure | Relatórios enriquecidos (@Epic, @Feature, @Story) | `make allure-serve` |
| Checkstyle | Google Java Style Guide | `make lint` |
| PMD | Complexidade ciclomática e código morto | `make lint` |
| SpotBugs | Null dereference e resource leaks | `make lint` |
| Spotless | Auto-formatação (pre-commit hook) | `make format` |
| SonarQube | Dashboard consolidado | `make sonar` |

### Total de Testes
**1052+ testes** — `./mvnw clean test` → BUILD SUCCESS

---

## Infraestrutura de Desenvolvimento (Fase 1)

### Docker Compose
| Serviço | Porta | Descrição |
|---------|-------|-----------|
| `mecanica-app` | 8080 | API + Swagger UI |
| `mecanica-postgres` | 5433 | PostgreSQL 16 |
| `mecanica-adminer` | 8081 | Browser de banco |
| `mecanica-mailhog` | 8025/1025 | Email catcher (SMTP dev) |
| `sonarqube` | 9000 | Análise de qualidade |

### Banco de Dados
- **PostgreSQL 16** em produção / **H2 in-memory** em testes
- **Flyway** — 15 migrações versionadas (V1 → V15)
- Seeding automático em dev (`seeding.enabled: true`)

### Usuários Seeded (dev)
| Role | Email | Senha |
|------|-------|-------|
| Admin | `admin@mecanica.com` | `123456` |
| Mecânico | `mecanico@mecanica.com` | `123456` |
| Atendente | `atendente@mecanica.com` | `123456` |
| Cliente | `cliente@mecanica.com` | `123456` |

---

## ADRs Documentados na Fase 1

31 Architecture Decision Records em `docs/ADRs/` — criados durante a Fase 1 (ADR-001 a ADR-028) e complementados na Fase 2 (ADR-029 a ADR-031).

### ADRs de Maior Impacto

| ADR | Título | Impacto |
|-----|--------|---------|
| ADR-003 | Task-based OS Status API | Endpoints de ação como sub-recursos: `/ordens-servico/{id}/acoes` |
| ADR-010 | Estratégia de Autorização Híbrida | JWT roles + `@OsSecurity` (object-level) — clientes só veem suas OS |
| ADR-019 | Desacoplamento Orçamento-Estoque | Estoque baixado na aprovação da OS, não do orçamento |
| ADR-022 | Inversão Fluxo OS/Orçamento | Cliente aprova orçamento → OS avança para APROVADA |
| ADR-008 | Padronização de Exceções | Código de erro `"OS-422-01"` em todas as exceções de domínio |
| ADR-015 | Geração de PDF | Flying Saucer + OpenPDF — acionado por domain event `OsFinalizadaEvent` |
| ADR-016 | Sistema de Emails | Spring Mail + Thymeleaf — MailHog em dev |
| ADR-027 | Estratégia de Testes Integrados | TestContainers com PostgreSQL real — sem H2 em testes de integração |

---

## Estrutura de Pacotes

```
com/fiap/mecanica/
├── domain/
│   ├── model/          # Entidades e Value Objects — zero Spring/JPA
│   ├── repository/     # Ports (interfaces) — contratos para infra
│   ├── exception/      # DomainRuleException, BusinessException, ResourceNotFoundException
│   └── event/          # Domain events (Spring ApplicationEvent)
│
├── application/
│   ├── service/        # Interfaces de use case
│   ├── service/impl/   # Implementações dos use cases
│   ├── dto/            # DTOs internos de aplicação
│   └── listener/       # @TransactionalEventListener + @Async
│
├── infra/
│   ├── adapter/        # Jpa*RepositoryAdapter — implementam ports do domínio
│   ├── entity/         # JPA entities — mapeadas para/do domínio por mappers
│   ├── jpa/            # Spring Data interfaces (usadas apenas pelos adapters)
│   ├── mapper/         # domain model ↔ JPA entity
│   ├── config/         # SecurityConfig, JwtConfig, AppConfig
│   ├── security/       # JwtFilter, JwtService, OsSecurity, ApiKeyFilter
│   └── seeding/        # Seeding de dados para ambiente dev
│
└── presentation/
    ├── api/            # *ControllerApi interfaces — contratos OpenAPI
    ├── controller/     # REST controllers — implementam interfaces *Api
    ├── dto/            # Request/Response DTOs
    ├── mapper/         # domain model ↔ request/response DTO
    └── handler/        # GlobalExceptionHandler
```

---

## Padrão de Dependências (Regra de Ouro)

```
presentation  →  application  →  domain
     ↓                              ↑
   infra  ────────────────────────────
```

- **domain** não conhece: Spring, JPA, HTTP, email — POJO puro Java
- **application** conhece: domain, Spring (eventos, @Service, @Transactional)
- **infra** conhece: domain ports + Spring Data + JPA + Security
- **presentation** conhece: application services + Spring MVC

---

## Artefatos de Documentação

| Artefato | Localização |
|----------|-------------|
| Linguagem Ubíqua (glossário DDD) | `docs/arquitetura/linguagem_ubiqua.md` |
| Event Storming | `docs/arquitetura/event_storming.md` |
| Diagramas C4 (Bounded Contexts) | `docs/arquitetura/diagramas.md` |
| Arquitetura completa | `docs/arquitetura.md` |
| Qualidade e testes | `docs/qualidade.md` |
| Decisões técnicas (índice ADRs) | `docs/decisoes.md` |
| ADRs individuais | `docs/ADRs/ADR-001-*.md` ... `ADR-031-*.md` |
| Insomnia collection | `docs/api/Insomnia_export.yaml` |

---

## Checklist Final Fase 1

- [x] Domínio puro (zero Spring/JPA em `domain/`) — verificado
- [x] Arquitetura Hexagonal com ports & adapters — 282 classes
- [x] Máquina de estados da OS com 8 estados (7 + CANCELADA)
- [x] Orçamentos com link HMAC-SHA256 para aprovação por email
- [x] Controle de estoque com reserva na aprovação do orçamento
- [x] Autenticação JWT + autorização híbrida (role + object-level)
- [x] Rate limiting via Bucket4j
- [x] Geração de PDF de entrega (Flying Saucer + OpenPDF)
- [x] Envio de emails com Thymeleaf (MailHog em dev)
- [x] 1052+ testes — JaCoCo 90%/80% — Checkstyle/PMD/SpotBugs/SonarQube
- [x] Swagger UI completo com 15 interfaces *Api documentadas
- [x] Insomnia collection com fluxo E2E completo
- [x] 28 ADRs documentando todas as decisões arquiteturais relevantes
- [x] Event Storming, Linguagem Ubíqua e Diagramas C4
- [x] Docker Compose com 5 serviços para desenvolvimento local
- [x] Seeding automático de usuários e dados de teste em dev
