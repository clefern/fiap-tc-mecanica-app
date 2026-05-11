# Infraestrutura — Mecânica API

Este documento descreve toda a infraestrutura do projeto: containerização, orquestração Kubernetes, Infrastructure as Code (Terraform), pipelines CI/CD e variáveis de ambiente.

---

## Desenvolvimento Local (Docker Compose)

**Pré-requisito:** Docker Desktop

```bash
make dev            # build + sobe todos os serviços
make logs           # acompanhar logs da aplicação em tempo real
make clean-docker   # reset completo (remove containers e volumes)
```

**Serviços disponíveis:**

| Serviço | Porta | URL / Acesso |
|---------|-------|-------------|
| API + Swagger | 8080 | http://localhost:8080/swagger-ui.html |
| PostgreSQL | 5433 | user: `mecanica_user` · pass: `mecanica_pass` · db: `mecanica` |
| MailHog (emails) | 8025 (web) / 1025 (SMTP) | http://localhost:8025 — intercepta todos os emails |
| Adminer (banco) | 8081 | http://localhost:8081 — server: `postgres` |
| SonarQube | 9000 | http://localhost:9000 — admin / admin |

**Comandos Maven (requer `./mvnw`):**

```bash
make test               # testes unitários
make integration-test   # testes de integração (TestContainers + PostgreSQL real)
make coverage           # JaCoCo → target/site/jacoco/index.html
make allure-serve       # relatório Allure interativo
make lint               # Checkstyle + PMD + SpotBugs
make format             # Spotless (auto-fix)
```

> Para gerar `./mvnw` sem Maven local: `chmod +x mvn-docker.sh && ./mvn-docker.sh -N org.apache.maven.plugins:maven-wrapper-plugin:3.2.0:wrapper`

### Containerização (Dockerfile)

- Multi-stage build para otimização de tamanho (~200MB final)
- Imagem base: Eclipse Temurin 21 (JRE Alpine)
- Usuário não-root para segurança
- Healthcheck integrado
- Otimizações JVM para containers

```bash
docker build -t mecanica-app:latest .

docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  mecanica-app:latest
```

### Docker Compose

- Networks isoladas
- Healthchecks configurados em todos os serviços
- Resource limits (CPU/Memory)
- Volumes persistentes para PostgreSQL

```bash
docker compose up -d --build    # subir tudo
docker compose logs -f app      # logs da aplicação
docker compose down             # parar
docker compose down -v          # parar + remover volumes
docker exec -it mecanica-app sh # shell no container
```

---

## Kubernetes

### Estrutura de Manifestos

```
k8s/
├── base/
│   ├── namespace.yaml            # Namespaces padrão
│   ├── configmap.yaml            # Variáveis não sensíveis
│   ├── secret.yaml               # Credenciais de exemplo
│   ├── postgres-pvc.yaml         # Storage do banco
│   ├── postgres-deployment.yaml  # Stateful deployment
│   ├── postgres-service.yaml     # ClusterIP
│   ├── app-deployment.yaml       # Spring Boot API
│   ├── app-service.yaml          # Service LoadBalancer
│   ├── mailhog-deployment.yaml   # SMTP fake
│   ├── mailhog-service.yml       # Interface Mailhog
│   ├── hpa.yaml                  # Autoscaling CPU/Mem
│   └── kustomization.yaml
└── overlays/
    ├── dev/
    │   ├── kustomization.yaml    # namespace mecanica-dev + namePrefix dev-
    │   ├── configmap-patch.yaml  # URLs/mails de dev
    │   └── deployment-patch.yaml # Ajustes de imagem/env
    └── prod/
        ├── kustomization.yaml
        └── deployment-patch.yaml # Resources + réplicas prod
```

### Recursos Kubernetes

- **Namespaces**: `mecanica` (base) e `mecanica-dev` via overlay
- **ConfigMaps**: base com JDBC + `SPRING_PROFILES_ACTIVE=prod`; dev patcha host/porta/mailhog/perfil `dev`
- **Secrets**: `mecanica-secret` com valores dummy — usar Sealed/External Secrets em produção
- **PostgreSQL**: 1 réplica, PVC 5Gi, probes TCP/HTTP, exposto via Service ClusterIP 5432
- **Aplicação**: requests `512Mi/500m`, limits `768Mi/1000m`, init container Busybox aguardando Postgres, probes `/actuator` (startup/liveness/readiness)
- **Mailhog**: deployment/service para captura de e-mails em dev
- **Services**: `postgres-service` (ClusterIP), `mecanica-service` (LoadBalancer 80→8080), `mailhog-service` (HTTP/SMTP)
- **HPA**: alvo CPU 70% / Memória 80%, range 2–10 réplicas; overlays ajustam réplicas dev (1) via `replicas` do Kustomize

**K8s Secrets necessários:** `DB_USER`, `DB_PASSWORD`, `JWT_SECRET_KEY`, `INTEGRATION_API_KEY`

### Deploy e verificação

```bash
# Minikube local
minikube start --cpus=4 --memory=8192
kubectl apply -k k8s/overlays/dev/
kubectl get all -n mecanica-dev
kubectl port-forward svc/dev-mecanica-service 8080:80 -n mecanica-dev

# EKS (produção)
aws eks update-kubeconfig --region us-east-1 --name mecanica-cluster
kubectl apply -k k8s/overlays/prod/
kubectl get all -n mecanica
kubectl get hpa -n mecanica
```

### Comandos úteis

```bash
kubectl logs -f deployment/mecanica-app -n mecanica
kubectl describe pod <pod-name> -n mecanica
kubectl exec -it deployment/mecanica-app -n mecanica -- sh
kubectl scale deployment mecanica-app --replicas=5 -n mecanica
kubectl rollout undo deployment/mecanica-app -n mecanica
kubectl rollout history deployment/mecanica-app -n mecanica
```

Ver [`k8s/README.md`](../k8s/README.md) para detalhes de cada manifesto.

---

## Terraform (AWS)

### Estrutura

```
infra/
├── conf/                      # Módulo raiz
│   ├── 0-providers.tf         # AWS + Helm
│   ├── 1-6-*.tf               # VPC, sub-redes, NAT, rotas
│   ├── 7-eks.tf               # Cluster EKS + addons
│   ├── 8-nodes.tf             # Node group
│   ├── 9-helm-provider.tf     # Config Helm
│   ├── 10-metrics-server.tf   # Chart metrics-server
│   ├── 11-albc.tf             # AWS Load Balancer Controller
│   ├── 12-ngnixc.tf           # Ingress NGINX com overlays
│   ├── 13-cert-manager.tf     # Cert-Manager
│   ├── 14-ecr.tf              # ECR para imagens
│   ├── data.tf / locals.tf    # Reuso de LabRole + tags globais
│   ├── values/                # Values padrão (metrics/nginx)
│   ├── iam/                   # Política do ALB Controller
│   └── output.tf / variables.tf
├── environments/
│   ├── dev/main.tf            # Chamada do módulo conf
│   ├── lab/main.tf            # Ambiente AWS Academy (LabRole)
│   └── local/                 # Template local
└── README.md                  # Guia e limitações
```

### Recursos provisionados

1. **VPC** dedicada (CIDR 10.0.0.0/16) com sub-redes públicas/privadas, NAT Gateway, IGW e rotas
2. **EKS** cluster + addons oficiais (coredns, kube-proxy, vpc-cni, pod-identity-agent)
3. **Node group** `general` (t3.large) com políticas workers/ECR; em `env=lab` reutiliza a `LabRole` existente
4. **Helm releases**: Metrics Server, AWS Load Balancer Controller, NGINX Ingress (com `values/` e overlays por ambiente) e Cert-Manager
5. **ECR** — registry de imagens Docker
6. **Outputs** principais: VPC ID/CIDR, subnets privadas e nome do cluster

### Variáveis principais

| Variável | Descrição | Default |
|----------|-----------|---------|
| `env` | Ambiente (dev, lab, prod) | — |
| `profile` | Perfil AWS CLI | — |
| `region` | Região AWS | `us-east-1` |
| `eks_name` | Nome base do cluster | `cluster-fiap` |
| `eks_version` | Versão do Kubernetes | `1.33` |
| `tags` | Tags adicionais aplicadas aos recursos | `{}` |

### Execução

```bash
# Dev
cd infra/environments/dev
cp secrets.tfvars.example secrets.tfvars   # preencher credenciais AWS
terraform init
terraform plan -var-file="terraform.tfvars" -var-file="secrets.tfvars"
terraform apply -var-file="terraform.tfvars" -var-file="secrets.tfvars"

# Lab (reutiliza LabRole e overlays locais)
cd infra/environments/lab
terraform init
terraform apply
```

**Observações:**
- Habilite o backend S3 comentado em `main.tf` quando rodar em contas persistentes
- Overlays em `environments/<env>/overlays/values/*.yaml` são detectados automaticamente e empilhados nos charts Helm (ex.: NodePort no lab)
- O ambiente AWS Academy/Lab possui permissões e quotas limitadas (detalhes em `infra/README.md`)

### Comandos úteis

```bash
terraform fmt -recursive   # formatar código
terraform validate         # validar configuração
terraform show             # ver state
terraform output           # ver outputs
terraform destroy \        # destruir (CUIDADO!)
  -var-file="environments/dev/terraform.tfvars" \
  -var-file="environments/dev/secrets.tfvars"
```

Ver [`infra/README.md`](../infra/README.md) para variáveis, outputs e troubleshooting.

---

## CI/CD (GitHub Actions)

### Estrutura de Workflows

```
.github/workflows/
├── ci.yml       # Build e testes unitários
├── test.yml     # Testes de integração
├── build.yml    # Docker multi-stage + Trivy scan + push ECR
├── cd.yml       # Deploy EKS
├── deploy.yml   # Deploy Kubernetes
└── infra.yml    # Provisionamento Terraform
```

### Pipelines

| Workflow | Gatilho | Ação |
|----------|---------|------|
| `ci.yml` | PR / push | Maven build, testes unitários, JaCoCo |
| `test.yml` | PR | Testes de integração (TestContainers) |
| `build.yml` | Push `develop`/`main` | Docker multi-stage + Trivy scan + push ECR |
| `cd.yml` | Após build | Deploy EKS via `kubectl apply -k` + smoke test |
| `infra.yml` | Manual | `terraform plan` / `terraform apply` |

### GitHub Secrets necessários

Configure em: `Settings → Secrets and variables → Actions`

| Secret | Descrição |
|--------|-----------|
| `AWS_ACCESS_KEY_ID` | Credenciais AWS |
| `AWS_SECRET_ACCESS_KEY` | Credenciais AWS |
| `AWS_SESSION_TOKEN` | Token de sessão (AWS Academy) |
| `ECR_REGISTRY` | URL do ECR (ex: `123456789.dkr.ecr.us-east-1.amazonaws.com`) |
| `ECR_REPOSITORY` | Nome do repositório ECR |
| `EKS_CLUSTER_NAME` | Nome do cluster EKS |
| `KUBE_CONFIG_DEV` | Kubeconfig do ambiente dev |
| `KUBE_CONFIG_PROD` | Kubeconfig do ambiente prod |
| `JWT_SECRET` | Chave HMAC para JWTs |
| `INTEGRATION_API_KEY` | API key M2M |
| `SONAR_TOKEN` | Token do SonarCloud |

### Criar Kubeconfig Secret

```bash
cat ~/.kube/config | base64
gh secret set KUBE_CONFIG_DEV < ~/.kube/config
```

### Comandos GitHub CLI

```bash
gh workflow list               # listar workflows
gh run list                    # ver runs
gh run view <run-id>           # detalhes de um run
gh run watch <run-id>          # acompanhar em tempo real
gh workflow run cd.yml -f environment=dev  # trigger manual
```

---

## Ambientes

| Ambiente | Namespace | Replicas | HPA | Storage |
|----------|-----------|----------|-----|---------|
| Dev | mecanica-dev | 1 | — | 2Gi |
| Prod | mecanica | 3 | 2–10 | 10Gi |

---

## Variáveis de Ambiente

| Variável | Descrição | Obrigatório em Prod |
|----------|-----------|:-------------------:|
| `JWT_SECRET_KEY` | Chave HMAC para assinar JWTs (mín. 256 bits) | Yes |
| `DB_URL` | JDBC URL do PostgreSQL | Yes |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciais do banco | Yes |
| `INTEGRATION_API_KEY` | API key para endpoint M2M | Yes |
| `ACTION_TOKEN_SECRET` | Segredo HMAC para tokens de email | Yes |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo (`dev` · `prod`) | Recomendado |

> Em desenvolvimento, valores padrão são definidos no perfil `dev` do `application-dev.yml` — **nunca usar em produção.**

---

## Troubleshooting

### Pod não inicia
```bash
kubectl describe pod <pod-name> -n mecanica
kubectl logs <pod-name> -n mecanica
```

### HPA não funciona
```bash
kubectl get deployment metrics-server -n kube-system
kubectl top pods -n mecanica
```

### Database não conecta
```bash
kubectl get pods -l app=postgres -n mecanica
kubectl logs deployment/postgres -n mecanica
```

### Build falha
```bash
mvn clean verify
docker compose up -d postgres
```
