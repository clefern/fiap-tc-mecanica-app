# RFC-002 — Banco gerenciado para Fase 3

**Status:** Accepted (ver [ADR-035](../ADRs/ADR-035-rds-postgresql-gerenciado.md))
**Data:** 2026-05-22
**Autores:** Grupo 14SOAT

## Resumo

Fase 3 exige **banco gerenciado** substituindo o Postgres em pod K8s da Fase 2. Escolhemos **AWS RDS PostgreSQL 16** em `db.t3.small`.

## Critérios

| Critério | Peso |
|---|---|
| Compatibilidade com as 17 migrations Flyway existentes (Postgres dialect) | ⭐⭐⭐⭐⭐ |
| Backup automatizado + PITR | ⭐⭐⭐⭐⭐ |
| Custo dentro de AWS Academy | ⭐⭐⭐⭐ |
| Latência de dentro da mesma VPC | ⭐⭐⭐⭐ |
| Suporte a Multi-AZ (futuro prod) | ⭐⭐⭐⭐ |
| Integração simples com Terraform | ⭐⭐⭐⭐ |

## Alternativas

### AWS RDS PostgreSQL 16 (escolhida)

- Engine PostgreSQL 16, classe `db.t3.small`
- Multi-AZ desligado em lab, habilitar em prod
- Backup retenção 7 dias, encriptação at-rest via KMS default
- Subnet group em subnets privadas (mesma VPC do EKS)
- Security Group: ingress 5432 apenas do VPC CIDR

**Prós:**
- 100% compatível com migrations Flyway atuais (zero mudança)
- Snapshots/PITR/upgrade gerenciados
- Same-VPC = latência < 1ms para EKS
- Terraform `aws_db_instance` maduro
- Custo lab dentro do crédito AWS Academy

**Contras:**
- Não é serverless (paga mesmo idle)
- Vertical scaling exige restart

### AWS Aurora PostgreSQL (Serverless v2 ou Provisioned)

**Prós:** HA built-in, escala automática

**Contras:** custo ~3x maior (mínimo `db.r6g.large`), latência ligeiramente maior pra workload OLTP simples; HA fora do escopo Fase 3

### Neon PostgreSQL Serverless

**Prós:** branching de banco, escala a 0, custo baixo idle

**Contras:** fora da VPC AWS (VPC peering ou public access necessário); latência variável; menos previsível pra Lambda que precisa conexão estável

### Supabase / Render Postgres

**Prós:** setup trivial, dashboard web

**Contras:** fora da AWS, exige public access (não atende padrão de banco interno em subnet privada); free tier limitado

### Manter Postgres em pod K8s + PVC

**Prós:** zero custo extra; portável

**Contras:** **viola requisito Fase 3** ("banco gerenciado"); sem backup automatizado; PVC numa AZ é SPOF; não recomendado pra produção

### MySQL / MariaDB RDS

**Prós:** custo similar; conhecido

**Contras:** migrar 17 migrations Flyway de Postgres pra MySQL dialect (data types, JSONB, indexes parciais, etc.) — custo de mudança > benefício

### DynamoDB / DocumentDB

**Prós:** serverless de verdade (DynamoDB), escala global

**Contras:** mudança de paradigma SQL→NoSQL; reescrita massiva do app (JPA → DynamoDB SDK); inviável no escopo Fase 3

## Decisão

**AWS RDS PostgreSQL 16, `db.t3.small`, em VPC privada compartilhada com EKS.**

Registrada em [ADR-035](../ADRs/ADR-035-rds-postgresql-gerenciado.md). Implementação no repo `fiap-tc-mecanica-infra-db` (commit `0fc6a65`).

## Riscos

- Custo de prod (~US$ 32/mês básico, US$ 64 com Multi-AZ): documentar pra decisão final do grupo
- Senha do master em `random_password` (Terraform state) em vez de AWS Secrets Manager: dívida técnica conhecida — rotação manual hoje
- Bottleneck de conexões (default 100): planejar pgBouncer se aplicação escalar > 5 réplicas
