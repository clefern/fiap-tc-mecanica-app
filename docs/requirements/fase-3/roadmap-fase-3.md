# Roadmap Fase 3 — Status Consolidado

> Mecânica API — Tech Challenge FIAP (Grupo 14SOAT)
> Última atualização: 2026-05-04
> Fonte de requisitos: `requisitos-fase-3.md` (espelho do PDF oficial)

---

## Status global

| Categoria | Total | ✅ | ⚠️ | ❌ |
|---|---:|---:|---:|---:|
| 1. Autenticação & API Gateway | 3 | 0 | 1 | 2 |
| 2. Repositórios & CI/CD | 7 | 1 | 1 | 5 |
| 3. Infraestrutura | 5 | 3 | 1 | 1 |
| 4. Observabilidade | 5 | 0 | 2 | 3 |
| 5. Documentação Arquitetural | 5 | 2 | 2 | 1 |
| 6. Entregáveis externos | 3 | 0 | 0 | 3 |
| **Total** | **28** | **6** | **7** | **15** |

Legenda: ✅ concluído · ⚠️ parcial (existe base, falta complemento) · ❌ pendente

---

## 1. Autenticação e API Gateway

| ID | Item | Status | Evidência / Gap |
|----|------|:------:|-----------------|
| AUTH-001 | API Gateway implementado | ⚠️ | NGINX Ingress Controller via Helm (`infra/conf/12-ngnixc.tf`). Atende como gateway de borda, mas não está formalmente documentado como tal nem possui integração com Lambda |
| AUTH-002 | Rotas sensíveis com auth via CPF | ❌ | Hoje só OAuth2 password (email + senha) em `presentation/controller/AuthController` |
| AUTH-003 | Function Serverless (CPF → JWT) | ❌ | Inexistente. Reusar `domain/repository/ClienteRepository.findByDocumento(Documento)` e `infra/config/security/JwtService` (HS256, access + refresh) |

## 2. Estrutura de Repositórios e CI/CD

| ID | Item | Status | Evidência / Gap |
|----|------|:------:|-----------------|
| REPO-001 | Repo Lambda | ❌ | Não criado |
| REPO-002 | Repo Infra K8s (Terraform) | ❌ | Hoje em `/infra/` deste monorepo |
| REPO-003 | Repo Infra DB (Terraform) | ❌ | Hoje embutido em `/infra/conf/` (sem RDS); branch `rds` com trabalho iniciado |
| REPO-004 | Repo Aplicação | ⚠️ | `fiap-tc-mecanica-java` existe; ao separar precisa expurgar `/infra/` e `/k8s/` |
| BRANCH-001 | Branch `main` protegida + PR obrigatório | ❌ | Verificar em GitHub Settings dos 4 repos após o split |
| CICD-001 | CI automático (build + testes) | ✅ | `.github/workflows/{ci,test}.yml` — gatilhos push/PR em main, develop, feature/*, feat/* |
| CICD-002 | CD automático em homolog/prod | ⚠️ | `.github/workflows/{cd,infra,build,deploy}.yml` existem mas rodam via `workflow_dispatch` manual |

## 3. Infraestrutura

| ID | Item | Status | Evidência / Gap |
|----|------|:------:|-----------------|
| INFRA-001 | API Gateway | ⚠️ | NGINX Ingress (`infra/conf/12-ngnixc.tf`) — mesmo de AUTH-001 |
| INFRA-002 | Function Serverless | ❌ | Mesmo de AUTH-003 |
| INFRA-003 | Banco de Dados Gerenciado | ⚠️ | Branch `rds` com commit `b87e468 feat: add RDS resources and update provider configurations` (não mergeado em develop/main). Hoje develop/main rodam Postgres em pod K8s com PVC |
| INFRA-004 | Cluster Kubernetes escalável | ✅ | EKS 1.33 (`infra/conf/7-eks.tf`) + node group (`8-nodes.tf`) + HPA 1–10 réplicas (`k8s/base/hpa.yaml`) |
| INFRA-005 | Terraform para provisionamento | ✅ | `infra/conf/*.tf` (17 arquivos: VPC, subnets, NAT, EKS, ECR, ALBC, NGINX, Cert-Manager, Metrics Server, EBS CSI, OIDC) + 3 ambientes (`infra/environments/{develop,lab,local}`) |

## 4. Observabilidade

| ID | Item | Status | Evidência / Gap |
|----|------|:------:|-----------------|
| OBS-001 | APM (Datadog ou New Relic) | ❌ | Sem dependência nem agente. Decisão da ferramenta em aberto — escolher na execução |
| OBS-002 | Métricas (latência, CPU/memória, uptime) | ⚠️ | Spring Boot Actuator (`/health`, `/info`, `/metrics`) + Micrometer + `@MonitoredOperation` AOP em 14+ métodos críticos (`infra/monitoring/MonitoredOperationAspect.java`, métrica `mecanica.service.execution`). Falta exportar para o APM |
| OBS-003 | Alertas para falhas de processamento de OS | ❌ | A configurar no APM escolhido |
| OBS-004 | Logs JSON estruturados com correlação | ⚠️ | MDC + `X-Correlation-ID` ✅ (`infra/config/CorrelationIdFilter.java`, pattern `[%X{correlationId:-}]`); falta `logstash-logback-encoder` + appender JSON ativável por profile (prod/lab) |
| OBS-005 | Dashboards (volume OS, tempo por status, erros) | ❌ | Métricas existem; falta o painel no APM |

## 5. Documentação Arquitetural

| ID | Item | Status | Evidência / Gap |
|----|------|:------:|-----------------|
| DOC-001 | Diagrama de Componentes (visão de cloud) | ⚠️ | C4 Containers em `docs/arquitetura/diagramas.md` cobre app + Postgres + MailHog + Adminer. Precisa adicionar API Gateway, Lambda, RDS e APM |
| DOC-002 | Diagrama de Sequência | ⚠️ | Existe abertura de OS e aprovação de orçamento via email; falta o fluxo de auth CPF (cliente → API Gateway → Lambda → app) |
| DOC-003 | RFCs | ❌ | Pasta `docs/RFCs/` não existe. Mínimo sugerido: RFC-001 escolha de cloud, RFC-002 escolha do banco, RFC-003 estratégia de auth CPF |
| DOC-004 | ADRs | ✅ | 31 ADRs em `docs/ADRs/` (incluindo ADR-010 estratégia híbrida de autorização, ADR-023/024 monitoramento, ADR-025 logging, ADR-031 decisões da Fase 2) |
| DOC-005 | Justificativa banco + ER | ✅ | ADR-001 PostgreSQL com justificativa formal + ER Mermaid (13+ tabelas com PKs/FKs/UKs/índices, herança Joined Table) em `docs/arquitetura/diagramas.md` |

## 6. Entregáveis externos

| ID | Item | Status | Observação |
|----|------|:------:|-----------|
| EXT-001 | README.md em cada um dos 4 repos | ❌ | Após split (REPO-001..004) |
| EXT-002 | Vídeo demo ≤ 15 min | ❌ | Externo — equipe |
| EXT-003 | PDF de entrega no Portal do Aluno | ❌ | Externo — equipe; deve incluir confirmação de `soat-architecture` adicionado a todos os repos |

---

## Itens reaproveitáveis (atalhos para a implementação)

Componentes existentes que aceleram a execução do que falta:

- **`ClienteRepository.findByDocumento(Documento)`** — usar na Lambda para o lookup do cliente por CPF
- **`DocumentoFactory`** — converte string em VO `Cpf`/`Cnpj`, valida formato
- **`JwtService`** (HS256, access + refresh, revogação persistida em `RevokedTokenEntity`) — Lambda gera token com mesma estratégia/secret para a app validar transparentemente
- **`UserContext` / `JwtAuthenticationFilter`** — não precisa mudar; valida o JWT vindo da Lambda automaticamente
- **`MonitoredOperationAspect`** + métrica `mecanica.service.execution` — métricas já capturadas por método/serviço; restando exportar para o APM
- **`CorrelationIdFilter`** + MDC `correlationId` — base pronta para logs estruturados, basta plugar `logstash-logback-encoder`
- **Flyway** (16 migrations versionadas em `src/main/resources/db/migration/`) — roda transparentemente no RDS quando INFRA-003 for finalizado
- **Helm charts já provisionados via Terraform** (NGINX, ALB Controller, Cert-Manager, Metrics Server) — base pronta para integração com APM via DaemonSet

---

## Observações operacionais

- **Caminho crítico (P0)**: AUTH-002, AUTH-003, REPO-001..004, INFRA-002, INFRA-003, OBS-001, EXT-002, EXT-003
- **Quick wins (P1)**: OBS-004 (encoder JSON), CICD-002 (trigger automático), DOC-002 (sequência auth CPF), DOC-001 (revisar C4 com nuvem)
- **APM em aberto**: a escolha entre Datadog e New Relic deve considerar custo (Datadog cobra por host; New Relic tem free tier permanente de 100GB/mês) e familiaridade do time. Decisão será registrada como ADR/RFC quando feita.
