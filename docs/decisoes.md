# Decisões Arquiteturais (ADRs)

Todas as decisões técnicas relevantes do projeto são documentadas como **Architecture Decision Records (ADRs)**.

Cada ADR segue o formato: **Contexto → Decisão → Justificativa → Alternativas → Implicações**.

---

## Fase 1 — Core do Domínio

| ADR | Título | Tema |
|-----|--------|------|
| [ADR-001](./ADRs/ADR-001-database-choice.md) | Escolha do Banco de Dados (PostgreSQL) | Persistência |
| [ADR-002](./ADRs/ADR-002-validation-architecture.md) | Arquitetura de Validação | Integridade |
| [ADR-003](./ADRs/ADR-003-task-based-os-status-api.md) | API de Status de OS Baseada em Tarefas | API Design |
| [ADR-004](./ADRs/ADR-004-reorganizacao-controllers-os.md) | Reorganização dos Controllers de OS | RESTful Design |
| [ADR-005](./ADRs/ADR-005-servicos-adicionais-em-aprovacao.md) | Serviços Adicionais na Aprovação | Flexibilidade |
| [ADR-006](./ADRs/ADR-006-unificacao-validacao.md) | Unificação da Validação | DRY |
| [ADR-007](./ADRs/ADR-007-reforco-validacao.md) | Reforço de Validação | Segurança |
| [ADR-008](./ADRs/ADR-008-padronizacao-excecoes.md) | Padronização de Exceções | Error Handling |
| [ADR-009](./ADRs/ADR-009-implementacao-sonarqube.md) | Implementação do SonarQube | Qualidade Contínua |
| [ADR-010](./ADRs/ADR-010-estrategia-autorizacao-hibrida.md) | Estratégia de Autorização Híbrida | Segurança |
| [ADR-011](./ADRs/ADR-011-gestao-estoque-pecas.md) | Gestão de Estoque de Peças | Domínio |
| [ADR-012](./ADRs/ADR-012-sistema-priorizacao-os.md) | Sistema de Priorização de OS | Regras de Negócio |
| [ADR-013](./ADRs/ADR-013-automacao-correcao-warnings.md) | Automação de Correção de Warnings | Manutenibilidade |
| [ADR-014](./ADRs/ADR-014-gestao-automatica-orcamentos.md) | Gestão Automática de Orçamentos | Automação |
| [ADR-015](./ADRs/ADR-015-pdf-generation-storage.md) | Geração e Armazenamento de PDF | Documentos |
| [ADR-016](./ADRs/ADR-016-sistema-envio-emails.md) | Sistema de Envio de Emails | Notificações |
| [ADR-017](./ADRs/ADR-017-fluxo-uso-insomnia.md) | Fluxo de Uso do Insomnia | Developer Experience |
| [ADR-018](./ADRs/ADR-018-estrategia-testes-e2e.md) | Estratégia de Testes E2E | Qualidade |
| [ADR-019](./ADRs/ADR-019-desacoplamento-orcamento-estoque.md) | Desacoplamento Orçamento-Estoque | Bounded Contexts |
| [ADR-020](./ADRs/ADR-020-subdominio-prioridade-os.md) | Subdomínio de Prioridade de OS | DDD |
| [ADR-021](./ADRs/ADR-021-flexibilizacao-atribuicao-mecanico.md) | Flexibilização de Atribuição de Mecânico | Operacional |
| [ADR-022](./ADRs/ADR-022-inversao-fluxo-aprovacao-os-orcamento.md) | Inversão do Fluxo de Aprovação OS/Orçamento | Processo |
| [ADR-023](./ADRs/ADR-023-monitoramento-tempo-medio-servicos.md) | Monitoramento de Tempo Médio de Serviços | Observabilidade |
| [ADR-024](./ADRs/ADR-024-monitoramento-tempo-medio-os-endpoint.md) | Endpoint de Tempo Médio por Mecânico | Métricas |
| [ADR-025](./ADRs/ADR-025-arquitetura-logging.md) | Arquitetura de Logging | Rastreabilidade |
| [ADR-026](./ADRs/ADR-026-reprovacao-orcamento-impacto-os.md) | Reprovação de Orçamento e Impacto na OS | Gestão de Estado |
| [ADR-027](./ADRs/ADR-027-estrategia-testes-integrados.md) | Estratégia de Testes Integrados | Pirâmide de Testes |
| [ADR-028](./ADRs/ADR-028-reestruturacao-documentacao.md) | Reestruturação da Documentação | Organização |

---

## Fase 2 — Infraestrutura e Escalabilidade

| ADR | Título | Tema |
|-----|--------|------|
| [ADR-029](./ADRs/ADR-029-estrategia-testes-carga-interface.md) | Estratégia de Load Testing (k6) | Performance |
| [ADR-030](./ADRs/ADR-030-separacao-pipeline-ci-cd.md) | Separação dos Pipelines CI/CD | Automação |
| [ADR-031](./ADRs/ADR-031-decisoes-fase2.md) | Decisões Consolidadas da Fase 2 | Infra + API |
