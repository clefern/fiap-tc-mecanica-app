# ADR-033 — Traefik como API Gateway (substituindo NGINX Ingress)

**Status:** Accepted
**Data:** 2026-05-22
**Fase do projeto:** Fase 3 — Tech Challenge (Grupo 14SOAT)
**Decisores:** Grupo 14SOAT
**Substitui:** trecho de ADR-031 que referenciava NGINX Ingress como gateway de borda

## Contexto

A Fase 3 do Tech Challenge exige um **API Gateway** que faça controle, roteamento e (idealmente) integração com a Function Serverless de autenticação. Na Fase 2, o `fiap-tc-mecanica-infra-k8s` instalava o **NGINX Ingress Controller** via Helm e o usava como gateway de borda.

A inflexão veio quando começamos a desenhar a rota `/auth → API Gateway AWS → Lambda CPF→JWT`: NGINX trata API Gateway externo como `ExternalName` Service, e a manipulação de header Host + SNI para HTTPS-passthrough exige patches que ficam frágeis e mal-documentados em produção.

## Decisão

Substituir o NGINX Ingress Controller por **Traefik v3 (Helm chart `traefik/traefik:39.0.9`)** em `fiap-tc-mecanica-infra-k8s`.

### Topologia resultante

```
Cliente
   │
   ▼
[ Traefik IngressRoute ]   (k8s/base/app-ingressroute.yaml)
   ├── /auth   (priority 200) → ServersTransport SNI → API Gateway AWS → Lambda
   ├── /api    (priority 100) → mecanica-service (rate-limit middleware: 50 req/s, burst 100)
   └── /       (priority   1) → mecanica-service (default)
```

### Componentes em código

- `infra/conf/12-traefik.tf` — Helm release Traefik 39.0.9 (NLB, LoadBalancer, annotations AWS)
- `infra/conf/values/traefik.yaml` — values base
- `infra/environments/lab/overlays/values/traefik.yaml` — override lab (NodePort 30080/30443)
- `k8s/base/traefik-middleware.yaml` — middleware de rate limit
- `k8s/base/app-ingressroute.yaml` — IngressRoute com 3 rotas
- `k8s/base/traefik-lambda-gateway.yaml` — ServersTransport + middlewares pra Lambda API GW

## Alternativas consideradas

| Alternativa | Por que descartada |
|---|---|
| Manter NGINX Ingress | Trata API Gateway externo via `ExternalName` + patches Host header; frágil e mal-documentado |
| AWS API Gateway HTTP/REST como gateway único (sem ingress in-cluster) | Tira poder de roteamento avançado in-cluster; força tudo via AWS GW (custo, latência, perda de middlewares K8s) |
| AWS ALB Controller direto (sem Ingress separado) | Não tem middlewares (rate-limit, ForwardAuth) no plano L7 sem adicionar Lambda@Edge |
| Istio / Ambassador / Kong | Overkill pra escopo Fase 3; aumenta superfície operacional sem ganho real |

## Consequências

- **Positivas:**
  - Roteamento por priority + middlewares declarativos em YAML (rate-limit, ForwardAuth, headers)
  - Integração nativa com Cert-Manager (já provisionado) para TLS
  - Painel `traefik.dashboard` em dev pra debug
  - SNI passthrough para AWS API Gateway sem hacks de Host header
- **Negativas / dívidas:**
  - Aprendizado de IngressRoute CRDs (não é Ingress vanilla)
  - Helm release adiciona ~80MB ao cluster vs NGINX
  - Documentação da equipe precisa atualizar (`docs/infraestrutura.md`, README)

## Migração

O PR `refactor: migrate from NGINX to Traefik as API Gateway` (commit `db74633` em `infra-k8s`) fez a migração completa:
- Deletados: `infra/conf/12-nginx.tf`, `infra/conf/values/nginx-ingress.yaml`, overlay nginx-ingress
- Adicionados: arquivos Terraform e manifests listados acima

## Referências

- [Traefik v3 docs — IngressRoute](https://doc.traefik.io/traefik/v3.0/routing/providers/kubernetes-crd/)
- ADR-031 (Fase 2): trecho de gateway agora obsoleto
- IngressRoute em produção: `fiap-tc-mecanica-infra-k8s/k8s/base/app-ingressroute.yaml`
