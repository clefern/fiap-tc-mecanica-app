# Mecânica API

> Backend de sistema integrado para gestão de oficinas mecânicas.
> **Tech Challenge — Pós-Graduação em Arquitetura de Software (FIAP, Grupo 14SOAT)**

Java 21 · Spring Boot 3.3 · PostgreSQL · Arquitetura Hexagonal + DDD

---

## Fase 1 — Core do Domínio

Objetivo: MVP com foco em DDD, qualidade de código e segurança.

**O que foi entregue:**
- CRUD de clientes, veículos, serviços, peças/insumos e controle de estoque
- Ordens de Serviço com máquina de estados: `RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → APROVADA → EM_EXECUCAO → FINALIZADA → ENTREGUE` | `CANCELADA`
- Orçamentos gerados automaticamente; aprovação pelo cliente via email
- Priorização de OS por sistema de pesos
- Notificações assíncronas por email (Thymeleaf templates, MailHog em dev)
- Autenticação JWT com roles: `ADMIN`, `ATENDENTE`, `MECANICO`, `CLIENTE`
- Autorização híbrida: roles + object-level security (`@OsSecurity`)
- Monitoramento de tempo médio de execução por mecânico
- 1052+ testes (unitários + integração com TestContainers) — cobertura JaCoCo 90%/80%
- Análise estática: Checkstyle + PMD + SpotBugs + SonarQube

**Artefatos DDD:**
[Event Storming](./docs/arquitetura/event_storming.md) · [Linguagem Ubíqua](./docs/arquitetura/linguagem_ubiqua.md) · [Diagramas C4](./docs/arquitetura/diagramas.md)

---

## Fase 2 — Infraestrutura e Escalabilidade

Objetivo: containerização, orquestração Kubernetes na AWS, CI/CD e novas APIs.

**O que foi entregue:**
- Novos endpoints: abertura completa de OS, fila operacional, status leve, aprovação M2M
- Aprovação de orçamento via link tokenizado no email (HMAC-SHA256)
- Dockerfile multi-stage + docker-compose completo
- Kubernetes (EKS): HPA CPU 70% / Mem 80%, min 1 / max 10 réplicas
- Terraform: VPC, EKS 1.33, ECR, ALB Controller, NGINX Ingress, Metrics Server
- GitHub Actions: CI (Maven + JaCoCo) · Docker (Trivy + ECR) · Deploy (EKS)
- K8s Secrets para JWT, DB e API key

### Arquitetura de Infraestrutura

```
┌──────────────────────────────────────────────┐
│  Cliente / Browser                           │
└────────────────┬─────────────────────────────┘
                 │ HTTPS
┌────────────────▼─────────────────────────────┐
│  AWS ALB / NGINX Ingress                     │
└────────────────┬─────────────────────────────┘
                 │
┌────────────────▼─────────────────────────────┐
│  EKS Cluster — us-east-1 (VPC privada)       │
│                                              │
│  mecanica-app  Spring Boot 3.3 / Java 21     │
│  HPA: CPU 70% / Mem 80% → 1..10 réplicas    │
│                                              │
│  postgres  PostgreSQL 16 + PVC gp2 (EBS)    │
│                                              │
│  ConfigMap + Secret (DB, JWT, API key)       │
└────────────────┬─────────────────────────────┘
                 │
┌────────────────▼─────────────────────────────┐
│  AWS ECR — registry de imagens Docker        │
└──────────────────────────────────────────────┘

CI/CD: Push → Maven + testes → Docker + Trivy → ECR → kubectl apply
```

### Arquitetura da Aplicação (Hexagonal + DDD)

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation                         │
│  Controllers · Request/Response DTOs · *Api interfaces  │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                    Application                          │
│  Use Cases (Services) · DTOs · Event Listeners · Email  │
└────────────────────────┬────────────────────────────────┘
                         │  ← ports (interfaces)
┌────────────────────────▼────────────────────────────────┐
│                      Domain                             │
│  Entities · Value Objects · Ports · Exceptions · Events │
│  (zero Spring/JPA — POJO puro)                          │
└────────────────────────┬────────────────────────────────┘
                         │  ← adapters (implements ports)
┌────────────────────────▼────────────────────────────────┐
│                   Infrastructure                        │
│  JPA Adapters · JPA Entities · Mappers · Security · DB  │
└─────────────────────────────────────────────────────────┘
```

### Novos Endpoints (Fase 2)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/ordens-servico/abertura-completa` | OS completa: cliente + veículo + serviços + peças em um payload |
| `GET`  | `/api/ordens-servico/fila-operacional` | OS ativas ordenadas por status (execução > aguardando > diagnóstico > recebida) |
| `GET`  | `/api/ordens-servico/{id}/status` | Status leve da OS (sem itens) |
| `POST` | `/api/integracoes/orcamentos/aprovacao` | Aprovação/recusa externa de orçamento via API key (M2M) |

---

## Fase 3 — Operação corporativa: 4 repos, Lambda CPF, RDS, Traefik, OTel/New Relic

Objetivo: elevar a aplicação a nível de operação corporativa — autenticação serverless, banco gerenciado, API Gateway profissional, observabilidade ponta a ponta.

**O que foi entregue:**

- **Separação em 4 repositórios independentes** com CI/CD próprio:
  - [`fiap-tc-mecanica-app`](https://github.com/clefern/fiap-tc-mecanica-app) — esta aplicação Spring Boot
  - [`fiap-tc-mecanica-infra-k8s`](https://github.com/clefern/fiap-tc-mecanica-infra-k8s) — Terraform EKS + Traefik + OTel Collector + Kustomize
  - [`fiap-tc-mecanica-infra-db`](https://github.com/clefern/fiap-tc-mecanica-infra-db) — Terraform RDS PostgreSQL (consome VPC via `terraform_remote_state` do `infra-k8s`)
  - [`fiap-tc-mecanica-lambda`](https://github.com/clefern/fiap-tc-mecanica-lambda) — Function Serverless CPF→JWT (Node.js 20 + TypeScript + Terraform)
- **Autenticação de cliente via CPF** ([ADR-032](./docs/ADRs/ADR-032-autenticacao-cpf-via-lambda.md)): `POST /auth` no Traefik → Lambda valida CPF (módulo 11) → busca cliente no RDS → emite JWT HS256 com mesma `SECURITY_JWT_SECRET_KEY` do app. `JwtAuthenticationFilter` valida transparentemente. `AuthController.getToken` (email + senha) fica `@Deprecated` para clientes, mantido para staff interna.
- **Traefik como API Gateway** ([ADR-033](./docs/ADRs/ADR-033-traefik-api-gateway.md)) substituindo NGINX: IngressRoute com 3 rotas (`/auth → Lambda`, `/api → app` com rate-limit, `/ → default`).
- **RDS PostgreSQL gerenciado** ([ADR-035](./docs/ADRs/ADR-035-rds-postgresql-gerenciado.md)) substituindo Postgres em pod K8s; backup automatizado 7 dias, mesma VPC do EKS, secret group restritivo (ingress 5432 só do VPC CIDR).
- **Observabilidade OTel + New Relic** ([ADR-034](./docs/ADRs/ADR-034-observabilidade-opentelemetry-newrelic.md)):
  - OpenTelemetry Collector DaemonSet recebe OTLP + hostmetrics + kubeletstats; exporta pra New Relic (`https://otlp.nr-data.net`)
  - App tem OTel Java Agent v2.26.1 no Dockerfile + Micrometer OTLP exporter + Logback JSON
  - `MonitoredOperationAspect` instrumenta 14+ métodos críticos com tags `status/operation/service/error_type`
  - `CorrelationIdFilter` propaga `X-Correlation-ID` em logs estruturados (MDC)
  - Auditoria de OS: 3 eventos novos + `OsHistoryListener` + tabela `os_history` (migration V17) → métricas de ciclo de vida
  - Dashboards New Relic como código (2 páginas) + 3 alertas NRQL (high_error_rate, os_process_failure, app_downtime)
- **CI/CD reusable workflows**: build (ECR push + semver), deploy (rollout EKS com timeout), infra (Terraform apply orquestrado). Branch protection ativa em `main` e `develop` nos 4 repos (PR obrigatório, 1 approval mínimo).

**Fluxo de autenticação de cliente (Fase 3):**

```
Cliente                  Traefik              API Gateway AWS         Lambda             RDS              Mecânica App
  │                         │                       │                   │                  │                   │
  │── POST /auth {cpf} ────►│                       │                   │                  │                   │
  │                         │── ServersTransport ──►│                   │                  │                   │
  │                         │   (SNI + Host hdr)    │── invoke ────────►│                  │                   │
  │                         │                       │                   │── findByDoc ────►│                   │
  │                         │                       │                   │◄── Cliente ──────│                   │
  │                         │                       │                   │                                      │
  │                         │                       │                   │   sign JWT HS256                     │
  │                         │                       │                   │   (subject=email, role=CLIENTE)      │
  │                         │                       │◄── 200 JWT ───────│                                      │
  │                         │◄── 200 JWT ───────────│                                                          │
  │◄── 200 {access_token} ──│                                                                                  │
  │                                                                                                            │
  │── GET /api/clientes/documento/{cpf}  Authorization: Bearer <jwt> ─────────────────────────────────────────►│
  │                                                                                                            │── JwtAuthFilter
  │                                                                                                            │   valida HS256
  │                                                                                                            │   loadUserBy(email)
  │                                                                                                            │── @PreAuthorize
  │                                                                                                            │   isOwnerByDoc
  │◄── 200 {cliente} ──────────────────────────────────────────────────────────────────────────────────────────│
```

**Documentação adicional Fase 3:**

- [docs/ADRs/](./docs/ADRs/) — ADR-032 (auth CPF), ADR-033 (Traefik), ADR-034 (OTel+NR), ADR-035 (RDS)
- [docs/RFCs/](./docs/RFCs/) — RFC-001 (cloud), RFC-002 (banco), RFC-003 (auth CPF) — propostas com alternativas
- Diagramas C4 atualizados em [docs/arquitetura/diagramas.md](./docs/arquitetura/diagramas.md)

---

## Quick Start

**Pré-requisito:** Docker

```bash
git clone <repo> && cd mecanica
make dev
open http://localhost:8080/swagger-ui.html
```

**Usuários de teste** (seeding automático):

| Role | Email | Senha |
|------|-------|-------|
| Admin | admin@mecanica.com | 123456 |
| Mecânico | mecanico@mecanica.com | 123456 |
| Atendente | atendente@mecanica.com | 123456 |
| Cliente | cliente@mecanica.com | 123456 |

---

## Documentação

Ver o **[índice completo da documentação](./docs/README.md)** com guias por perfil (avaliador, desenvolvedor, DevOps, arquiteto).

| Documento | Conteúdo |
|-----------|----------|
| [Arquitetura](./docs/arquitetura.md) | Hexagonal, DDD, modelo de domínio, fluxos críticos |
| [Infraestrutura](./docs/infraestrutura.md) | Docker, Kubernetes, Terraform, CI/CD, variáveis de ambiente |
| [Qualidade e Testes](./docs/qualidade.md) | Pirâmide de testes, cobertura, análise estática, segurança |
| [Decisões Arquiteturais (ADRs)](./docs/decisoes.md) | 31 ADRs — histórico completo de decisões técnicas |
| [Manual de Execução](./docs/manual_execucao.md) | Quick start, fluxo de avaliação, SonarQube |
| [Insomnia Collection](./docs/api/Insomnia_export.yaml) | Coleção completa de APIs para testes manuais |
| [Swagger UI](http://localhost:8080/swagger-ui.html) | Documentação interativa (requer app rodando) |

---

## Stack

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Banco de Dados | PostgreSQL 16 |
| Arquitetura | Hexagonal + DDD + Clean Architecture |
| Migrations | Flyway |
| Segurança | JWT (JJWT 0.12.5) + Spring Security |
| Testes | JUnit 5, Mockito, TestContainers, RestAssured, Allure |
| Qualidade | Checkstyle, PMD, SpotBugs, Spotless, JaCoCo, SonarQube |
| Containers | Docker, Kubernetes (EKS) |
| IaC | Terraform (AWS) |
| CI/CD | GitHub Actions |

---

## Autores

Grupo **14SOAT** — FIAP 2025/2026

- Cleber Fernandes — cleber_sim@outlook.com
- Celio — celio.vetrano@gmail.com
- Jonnes Nascimento — jonnes.nascimento@gmail.com
- Thaís Souza — thais.azuoss@gmail.com
