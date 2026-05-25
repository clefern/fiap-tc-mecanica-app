# ADR-035 — RDS PostgreSQL gerenciado (substitui Postgres em pod com PVC)

**Status:** Accepted
**Data:** 2026-05-22
**Fase do projeto:** Fase 3 — Tech Challenge (Grupo 14SOAT)
**Decisores:** Grupo 14SOAT
**Substitui:** trecho de ADR-031 que aceitava Postgres em pod K8s + PVC como solução temporária da Fase 2

## Contexto

A Fase 3 do Tech Challenge exige **Banco de Dados Gerenciado** (RDS, Aurora, ou equivalente). Na Fase 2, o `fiap-tc-mecanica-infra-k8s` rodava Postgres como Deployment + PVC (`k8s/base/postgres-deployment.yaml`, `postgres-service.yaml`, `postgres-pvc.yaml`) — funcional pra labs mas com 3 problemas conhecidos: (1) sem backup automatizado, (2) sem Multi-AZ, (3) `PVC + StatefulSet` em EBS dá point-of-failure de zona.

## Decisão

Provisionar **AWS RDS PostgreSQL 16 (`db.t3.small`)** em repo dedicado `fiap-tc-mecanica-infra-db`, consumido pelo cluster EKS via VPC compartilhada (lida via `terraform_remote_state` do `infra-k8s`).

### Topologia

```
fiap-tc-mecanica-infra-k8s             fiap-tc-mecanica-infra-db
─────────────────────────              ─────────────────────────
VPC + subnets privadas      ◄─────────  data.terraform_remote_state.k8s
EKS + node group                       │  consome vpc_id, subnet_ids, vpc_cidr
                                       ▼
                                  AWS RDS PostgreSQL 16
                                  ├ db.t3.small (lab) → db.m5.large (prod planejado)
                                  ├ subnet_group: private subnets
                                  ├ security_group: ingress 5432 from VPC CIDR
                                  ├ multi_az: false (lab) → true (prod)
                                  ├ backup_retention: 7 dias
                                  └ random_password (Secrets Manager planejado)

                                  Outputs:
                                  ├ rds_endpoint  → consumido por:
                                  ├ rds_port       │  • k8s ConfigMap (DB_URL)
                                  ├ rds_db_name    │    via k8s-deploy.sh
                                  ├ rds_user       │  • Lambda /auth via
                                  └ rds_password   │    terraform_remote_state
                                        (sensitive)
```

### Refactor crítico do `infra-db`

Antes (snapshot Fase 2): `infra-db/infra/conf/` duplicava VPC, subnets, NAT, IGW, EKS, node group, Helm releases (≈16 arquivos `.tf`) — risco de drift entre os 2 repos.

Depois (commit `0fc6a65` em `infra-db/main`, autor Gabriel Schuina): apenas `versions.tf`, `providers.tf`, `variables.tf`, `locals.tf`, `remote_state.tf`, `rds.tf`, `output.tf`. Tudo o que é VPC/EKS lê do `data.terraform_remote_state.k8s`.

### Refactor crítico do `infra-k8s`

Antes: `k8s/base/postgres-*.yaml` provisionava Postgres em pod com PVC.

Depois: novo overlay `k8s/overlays/rds/` com:
- `configmap-patch.yaml`: `DB_URL=jdbc:postgresql://${RDS_HOST}:5432/mecanica?sslmode=require`
- `secret-patch.yaml`: `DB_USER`, `DB_PASSWORD` via envsubst
- `deployment-patch.yaml`: init container `wait-for-rds` (nc -z $RDS_HOST 5432)
- `scripts/k8s-deploy.sh`: auto-resolve `RDS_HOST` via `aws rds describe-db-instances --db-instance-identifier "${ENV}-mecanica-pg"` quando não vier por env var

## Alternativas consideradas

| Alternativa | Por que descartada |
|---|---|
| **Manter Postgres em pod + PVC** | Sem backup automatizado; PVC numa única AZ é SPOF; viola requisito Fase 3 ("banco gerenciado") |
| **Aurora PostgreSQL** | Custo ~3x maior (db.r6g.large mínimo); HA fora do escopo do Tech Challenge; performance equivalente pra workload de OS |
| **Neon serverless Postgres** | Não AWS-nativo; rede VPC peering exige setup adicional; menos previsível em latência da Lambda |
| **DocumentDB / DynamoDB** | Mudança de paradigma (NoSQL); reescrever queries Flyway/JPA não cabe no escopo |
| **RDS MySQL / MariaDB** | Já temos 17 migrations Flyway em Postgres dialect; mudar engine custaria mais que o ganho |

## Consequências

- **Positivas:**
  - Snapshots + PITR de 7 dias automáticos
  - Upgrade de versão e patching gerenciados pela AWS
  - Outputs Terraform compartilhados (Lambda + app pegam endpoint+credentials do mesmo state)
  - `psql` direto de qualquer pod no EKS funciona (mesma VPC)
- **Negativas / dívidas:**
  - Custo: ~US$ 30/mês (lab `db.t3.small`); prod precisa decidir tamanho real
  - `random_password` no state em vez de AWS Secrets Manager — débito conhecido (rotação manual)
  - Multi-AZ desabilitado em lab — habilitar em prod
  - Flyway roda nas 17 migrations no primeiro boot do pod app — coordenar com `terraform apply` do infra-db pra evitar race

## Implementação

- `fiap-tc-mecanica-infra-db` commit `0fc6a65 refactor core` + `1284622 chore: update README`
- `fiap-tc-mecanica-infra-k8s` overlay `k8s/overlays/rds/` + `scripts/k8s-deploy.sh` (commit `0f477f1`, PR #7 Aniusch)
- `fiap-tc-mecanica-lambda` consome `rds_endpoint`, `rds_master_username/password`, `rds_security_group_id` via `terraform_remote_state` (commit `78a7d8d`, PR #2 Aniusch)

## Validação

- ✅ `terraform plan` em `infra-db/infra/environments/lab` mostra apenas recursos RDS (zero VPC/EKS)
- ✅ Outputs do `infra-db` consumidos pelo Lambda em `lambda/infra/conf/1-remote-state.tf`
- ⏳ `terraform apply` real em lab — pendente, requer credenciais AWS Academy

## Referências

- ADR-001 (Database choice — PostgreSQL): mantida; só muda o "onde roda"
- ADR-031: trecho "Postgres em pod aceito como solução de Fase 2" agora obsoleto
- Repo: `fiap-tc-mecanica-infra-db`
- Refactor commit: [0fc6a65](https://github.com/clefern/fiap-tc-mecanica-infra-db/commit/0fc6a65)
