# RFC-001 — Escolha de cloud para Fase 3

**Status:** Accepted
**Data:** 2026-05-22
**Autores:** Grupo 14SOAT

## Resumo

Fase 3 do Tech Challenge exige cloud com **API Gateway**, **Function Serverless**, **Banco Gerenciado**, **Cluster Kubernetes** e **Terraform** pra IaC. Escolhemos **AWS** como provedor único.

## Critérios

| Critério | Peso | Por quê |
|---|---|---|
| Atendimento aos 5 requisitos da Fase 3 com serviços nativos | ⭐⭐⭐⭐⭐ | Bloqueante |
| Maturidade do Terraform provider | ⭐⭐⭐⭐⭐ | Já temos código TF da Fase 2 |
| Custo dentro de free tier / créditos AWS Academy | ⭐⭐⭐⭐ | Sem orçamento extra |
| Curva de aprendizagem do time | ⭐⭐⭐⭐ | Conhecimento prévio |
| Documentação e comunidade | ⭐⭐⭐ | |

## Alternativas

### AWS (escolhida)

- **API Gateway**: AWS API Gateway HTTP/REST (free tier 1M reqs/mês), exposto via NLB+Traefik para `/auth`
- **Serverless**: AWS Lambda (free tier 1M reqs + 400k GB-s/mês)
- **DB Gerenciado**: AWS RDS PostgreSQL (free tier db.t3.micro 750h/mês — usamos t3.small em lab)
- **K8s**: AWS EKS 1.33 (não free; usamos crédito AWS Academy)
- **IaC**: Terraform AWS provider (maduro, ~6.32.0)
- **Observabilidade**: New Relic free tier 100GB/mês via OTel Collector

**Prós:**
- Todos os 5 requisitos com serviços first-party
- Terraform da Fase 2 já em AWS
- AWS Academy fornece créditos pro lab
- Documentação extensa

**Contras:**
- Custo de prod fora do AWS Academy (~US$ 200-400/mês com EKS + RDS Multi-AZ)
- Lock-in moderado (Lambda + API Gateway são proprietários)

### GCP

- **API Gateway**: Google API Gateway / Cloud Endpoints
- **Serverless**: Cloud Functions / Cloud Run
- **DB**: Cloud SQL PostgreSQL
- **K8s**: GKE
- **IaC**: Terraform google provider

**Prós:** GKE Autopilot é mais barato; BigQuery free tier generoso (não relevante aqui)

**Contras:** time sem experiência prática; reescrever TF da Fase 2

### Azure

- **API Gateway**: Azure API Management
- **Serverless**: Azure Functions
- **DB**: Azure Database for PostgreSQL Flexible Server
- **K8s**: AKS
- **IaC**: Terraform azurerm provider

**Prós:** crédito pra estudantes via Microsoft Learn

**Contras:** menor familiaridade do time; ferramentas serverless menos polidas que AWS Lambda

### Multi-cloud (ex: EKS + Lambda em AWS + DB em Neon)

**Prós:** flexibilidade

**Contras:** custo de complexidade (VPC peering, secrets sincronizadas, observabilidade fragmentada) não compensa pro Tech Challenge

## Decisão

**AWS.** Atende 5/5 requisitos nativamente, time tem familiaridade, código TF da Fase 2 reaproveitado. ADRs subsequentes (-033 Traefik, -034 OTel+NR, -035 RDS) documentam as escolhas específicas dentro do ecossistema AWS.

## Custos estimados (lab + prod)

| Recurso | Lab (AWS Academy) | Prod (estimado) |
|---|---|---|
| EKS cluster | crédito | ~US$ 73/mês |
| Node group 2× t3.large | crédito | ~US$ 121/mês |
| RDS db.t3.small | crédito | ~US$ 32/mês (Multi-AZ: ~US$ 64) |
| Lambda | crédito (free tier) | < US$ 1/mês |
| API Gateway | crédito (free tier) | < US$ 5/mês |
| New Relic | free tier 100GB | free tier |
| NAT Gateway + data transfer | crédito | ~US$ 35/mês |
| **Total estimado** | **US$ 0 (lab)** | **~US$ 270-300/mês** |

## Riscos

- Crédito AWS Academy esgotar → fallback: rodar tudo em Minikube local + LocalStack pra Lambda
- Lock-in Lambda → mitigado por OTel (telemetria portável) e Terraform (recriação rápida em outra cloud)
