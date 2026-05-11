# ADR-001: Escolha do Banco de Dados (PostgreSQL)

## Contexto
O sistema de oficina mecânica exige consistência transacional forte, modelagem relacional para entidades e agregados (OS, Cliente, Veículo, Serviços, Peças), além de recursos para dados semi-estruturados e extensibilidade. Precisamos de uma solução madura, com ótimo suporte em containers e ferramentas de migração.

## Decisão
Escolher **PostgreSQL** como banco de dados principal para a V1.

## Justificativa
- Consistência & ACID: Transações confiáveis para operações críticas (aprovação de orçamento, reserva/consumo de peças).
- Modelagem Relacional: FKs, constraints e índices para garantir integridade entre agregados (Cliente–Veículo, OS–Itens).
- JSONB & Extensões: Suporte a dados semi-estruturados e extensões úteis (pgcrypto para UUID).
- Performance e Índices: B-tree, GIN/GIST, partial indexes e outros recursos para consultas eficientes.
- Ecossistema maduro: Flyway, TestContainers, drivers JDBC estáveis, ampla comunidade.
- Container-friendly: Imagem oficial, healthcheck, fácil provisioning via Docker Compose.

## Alternativas Consideradas
- MySQL/MariaDB: Bom suporte relacional, porém PostgreSQL oferece recursos avançados (CTEs, window functions, JSONB) e sem limitações de tipos comparado a MySQL.
- MongoDB (NoSQL): Flexível para documentos, mas perderíamos garantias relacionais e transacionais fortes exigidas para estoques e orçamentos.
- SQLite: Simples para dev local, porém não atende concorrência e cenários de produção.

## Implicações
- Migrações versionadas com Flyway (baseline criada em `V1__baseline.sql`).
- Testes de integração com TestContainers em fases futuras.
- Observabilidade de queries e tuning em fases de performance (Fase 3).

## Status
Aceito para MVP (V1). Será reavaliado com ADR na Fase 3 para escalabilidade e eventuais necessidades de sharding/replicação.