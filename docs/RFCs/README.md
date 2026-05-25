# RFCs — Mecânica API

Request for Comments: propostas de decisões técnicas relevantes que envolvem trade-offs significativos.

Diferença entre RFC e ADR:
- **RFC** = proposta com alternativas discutidas, escrita ANTES da decisão; convida revisão.
- **ADR** = registro IMUTÁVEL do que foi decidido, escrita DEPOIS da decisão.

Quando uma RFC é aceita, a decisão final é referenciada via um ADR correspondente em `../ADRs/`.

| ID | Tema | Status | ADR ligado |
|---|---|---|---|
| [RFC-001](RFC-001-escolha-cloud.md) | Escolha de cloud para Fase 3 | Accepted | — |
| [RFC-002](RFC-002-banco-gerenciado.md) | Banco gerenciado: RDS PostgreSQL vs alternativas | Accepted | [ADR-035](../ADRs/ADR-035-rds-postgresql-gerenciado.md) |
| [RFC-003](RFC-003-estrategia-auth-cpf.md) | Estratégia de autenticação de cliente por CPF | Accepted | [ADR-032](../ADRs/ADR-032-autenticacao-cpf-via-lambda.md) |
