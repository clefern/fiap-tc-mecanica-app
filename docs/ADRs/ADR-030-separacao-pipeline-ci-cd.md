# ADR-030: Separação da Pipeline CI/CD para Ambiente AWS Academy

## Status

Aceito — Atualizado em 2026-03-21

## Contexto
O projeto utiliza GitHub Actions para automação de testes, provisionamento de infraestrutura (Terraform), build de imagem Docker (ECR) e deploy (EKS). O ambiente de nuvem utilizado é o **AWS Academy**, que possui limitações importantes:

1. **Credenciais temporárias** — as chaves de acesso (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`) expiram periodicamente e precisam ser atualizadas manualmente nos Secrets do repositório.
2. **Repositório privado no plano Free** — o GitHub não oferece **Environments com Required Reviewers** (aprovação manual no meio da pipeline) para repositórios privados no plano gratuito.
3. **Pipeline unificada falhava constantemente** — quando testes, infra, build e deploy estavam no mesmo workflow, qualquer push disparava toda a cadeia. Como as credenciais AWS expiram com frequência, os jobs de infra/build/deploy falhavam mesmo quando os testes passavam, gerando ruído e histórico de falhas desnecessárias.
4. **Necessidade de re-deploy rápido** — Durante correções de bugs ou ajustes de configuração, precisamos fazer deploy
   sem recriar toda a infraestrutura.

## Decisão
Separar a pipeline em **dois workflows independentes** com responsabilidades distintas:

### 1. CI — Testes Automáticos (`ci.yml`)
- **Trigger**: automático em `push` (branches `main`, `develop`, `feature/**`, `feat/**`) e `pull_request` para `main`.
- **Responsabilidade**: executar os testes unitários do projeto.
- **Não depende de credenciais AWS** — nunca falha por causa de infraestrutura.

### 2. CD — Provision & Deploy (`cd.yml`)
- **Trigger**: **manual** via `workflow_dispatch` (aba Actions → "CD" → Run workflow).
- **Responsabilidade**: executar em sequência:
  1. **Infra** (`infra.yml`) — provisiona recursos na AWS via Terraform (ECR, EKS, etc.).
  2. **Build & Push** (`build.yml`) — empacota a aplicação, cria a imagem Docker e envia ao Amazon ECR.
  3. **Deploy** (`deploy.yml`) — aplica os manifests Kubernetes e atualiza a imagem no EKS.
- **Depende de credenciais AWS válidas** — só deve ser disparado após atualizar os Secrets.

### 3. Deploy Only (`deploy.yml`)

- **Trigger**: **manual** via `workflow_dispatch` OU chamado pelo `cd.yml` via `workflow_call`.
- **Responsabilidade**: aplica apenas os manifestos Kubernetes no cluster EKS.
- **Uso**: re-deploy rápido após correções de bugs ou ajustes de configuração, sem reconstruir imagem ou infraestrutura.
- **Inputs obrigatórios** (quando executado manualmente):
  - `environment`: Ambiente (lab, prod)
  - `aws-region`: Região AWS (default: us-east-1)
  - `eks-cluster-name`: Nome do cluster (default: lab-fiap-cluster)

### 4. Cleanup Environment (`cleanup.yml`)

- **Trigger**: **manual** via `workflow_dispatch`.
- **Responsabilidade**: destruir completamente um ambiente:
  1. Terraform destroy (remove EKS, VPC, ECR, etc.)
  2. Remove bucket S3 do Terraform state
  3. Remove tabela DynamoDB de lock
- **Uso**: limpar ambiente para economia de recursos ou reset completo.
- **Input**: `environment` (develop, lab)

### Workflows Reutilizáveis

Os arquivos `test.yml`, `build.yml`, `deploy.yml` e `infra.yml` são workflows reutilizáveis (`workflow_call`). Podem ser
chamados por orquestradores (`ci.yml`, `cd.yml`) **OU** executados manualmente quando suportam `workflow_dispatch`.

### Estrutura dos Arquivos

```
.github/workflows/
├── ci.yml          # Orquestrador CI — testes automáticos (push/PR)
├── cd.yml          # Orquestrador CD — infra + build + deploy (manual)
├── test.yml        # Reutilizável — executa testes Maven + JaCoCo
├── infra.yml       # Reutilizável — Terraform init/plan/apply (com cleanup on failure)
├── build.yml       # Reutilizável — Maven package + Docker build + ECR push
├── deploy.yml      # Híbrido — workflow_call OU workflow_dispatch para deploy manual
└── cleanup.yml     # Manual — Terraform destroy + cleanup S3/DynamoDB
```

### Scripts Auxiliares

```
scripts/
├── bootstrap_tf_lab.sh   # Cria bucket S3 e tabela DynamoDB para Terraform state
├── cleanup_tf_lab.sh     # Remove bucket S3 (todas versões) e tabela DynamoDB
├── k8s-deploy.sh         # Script robusto de deploy K8s com Kustomize + validação
└── connect-eks.sh        # Conecta kubectl local ao cluster EKS na AWS
```

### Fluxo Visual

```
═══ CI (automático a cada push) ═══

  push/PR → [ 🧪 Testes ]  ✅ ou ❌
                              (fim)


═══ CD (manual via Actions) ═══

  Run workflow → [ 🏗️ Infra ] → [ 🐳 Build & Push ] → [ 🚀 Deploy ]
                     ↓ (on failure)
                 [ 🧹 Cleanup ]


═══ Deploy Only (manual via Actions) ═══

  Run workflow → [ 🚀 Deploy ]  (apenas manifests K8s)


═══ Cleanup (manual via Actions) ═══

  Run workflow → [ 💣 Terraform Destroy ] → [ 🗑️ Remove S3 + DynamoDB ]
```

### Variáveis e Secrets Necessários

Para que o workflow de CD funcione, é necessário configurar no repositório (Settings → Secrets / Variables):

**Secrets** (Settings → Secrets and variables → Actions → Secrets):

| Secret | Descrição |
|---|---|
| `AWS_ACCESS_KEY_ID` | Chave de acesso do AWS Academy |
| `AWS_SECRET_ACCESS_KEY` | Chave secreta do AWS Academy |
| `AWS_SESSION_TOKEN` | Token de sessão do AWS Academy (expira periodicamente) |

**Variables** (Settings → Secrets and variables → Actions → Variables):

| Variable             | Descrição                      | Exemplo               |
|----------------------|--------------------------------|-----------------------|
| `ENV`                | Ambiente padrão                | `lab`                 |
| `JAVA_VERSION`       | Versão do JDK                  | `21`                  |
| `AWS_REGION`         | Região da AWS                  | `us-east-1`           |
| `AWS_ACCOUNT_ID`     | ID da conta AWS                | `931061475590`        |
| `AWS_TF_BUCKET_NAME` | Bucket S3 para Terraform state | `fiap-tc-lab-tfstate` |
| `TF_PROFILE`         | Profile AWS para Terraform     | `fiap-lab`            |
| `IMAGE_TAG`          | Tag da imagem Docker           | `latest`              |

**Secrets Adicionais** (para aplicação):

| Secret                    | Descrição                  |
|---------------------------|----------------------------|
| `DB_USER`                 | Usuário do PostgreSQL      |
| `DB_PASSWORD`             | Senha do PostgreSQL        |
| `POSTGRES_USER`           | Usuário root do PostgreSQL |
| `POSTGRES_PASSWORD`       | Senha root do PostgreSQL   |
| `POSTGRES_DB`             | Nome do database           |
| `SECURITY_JWT_SECRET_KEY` | Chave secreta para JWT     |

> **⚠️ Importante**: quando as credenciais do AWS Academy expirarem, atualize os 3 Secrets AWS antes de disparar o
> workflow de CD. Caso contrário, os jobs de infra, build e deploy irão falhar.

## Detalhes Técnicos

### Script: k8s-deploy.sh

Script robusto que gerencia o deploy Kubernetes com as seguintes características:

1. **Validação de variáveis obrigatórias**: Verifica ENV, AWS_ACCOUNT_ID, AWS_REGION, credenciais DB, JWT secret
2. **Namespace automático**: Cria namespace `mecanica-${ENV}` antes da validação
3. **Substituição de variáveis**: Usa `envsubst` nos manifestos YAML antes do Kustomize build
4. **Kustomize**: Build dos manifestos finais com patches por ambiente
5. **Dry-run server**: Valida manifestos contra o API server antes de aplicar
6. **Rollout validation**: Aguarda deployment/statefulset ficarem prontos (timeout configurável)
7. **Cleanup automático**: Remove diretório temporário ao finalizar (sucesso ou falha)

**Convenções**:

- Namespace sempre segue o padrão `mecanica-${ENV}` (ex: `mecanica-lab`, `mecanica-prod`)
- Usa overlays do Kustomize em `k8s/overlays/${ENV}/`
- Base compartilhada em `k8s/base/`

### Script: bootstrap_tf_lab.sh

Cria infraestrutura necessária para o backend remoto do Terraform:

1. **Bucket S3**: Para armazenar o Terraform state (com versionamento habilitado)
2. **Tabela DynamoDB**: Para lock de state (`terraform-lock-table`)
3. **Idempotente**: Pode ser executado múltiplas vezes sem erro

### Script: cleanup_tf_lab.sh

Remove completamente a infraestrutura do backend Terraform:

1. **Esvazia bucket S3**: Remove todas as versões de objetos e delete markers
2. **Deleta bucket S3**: Remove o bucket completamente
3. **Deleta tabela DynamoDB**: Remove a tabela de lock
4. **Aguarda confirmação**: Usa `aws dynamodb wait table-not-exists`

**⚠️ Cuidado**: Este script é destrutivo e remove permanentemente o Terraform state!

### Script: connect-eks.sh

Conecta o kubectl local ao cluster EKS na AWS:

1. **Verifica pré-requisitos**: AWS CLI, kubectl, credenciais
2. **Atualiza kubeconfig**: Adiciona contexto do cluster EKS
3. **Testa conexão**: Mostra cluster-info, nodes, namespaces
4. **Detecta problemas**: Identifica pods crashados ou pendentes
5. **Sugere comandos**: Lista comandos úteis para debug

**Uso**:

```bash
./scripts/connect-eks.sh               # lab, us-east-1, lab-fiap-cluster
./scripts/connect-eks.sh prod          # prod, us-east-1, prod-fiap-cluster
./scripts/connect-eks.sh lab us-west-2 # lab, us-west-2, lab-fiap-cluster
```

## Consequências

### Positivas
- **Testes nunca falham por causa de infraestrutura** — o histórico do CI fica limpo e confiável.
- **Controle total sobre quando provisionar e deployar** — evita gastos desnecessários no AWS Academy.
- **Credenciais expiradas não geram ruído** — o CD só roda quando alguém decide disparar manualmente.
- **Workflows reutilizáveis** — cada responsabilidade está isolada em seu próprio arquivo, facilitando manutenção.
- **Deploy independente** — `deploy.yml` pode ser executado sozinho para re-deploy rápido após correções.
- **Cleanup automático on failure** — infraestrutura Terraform é destruída automaticamente se o provision falhar.
- **Scripts robustos** — validação de variáveis, dry-run, rollout status, cleanup automático.
- **Conexão local facilitada** — script `connect-eks.sh` simplifica debug e troubleshooting.

### Negativas
- **Deploy não é automático** — exige ação manual para disparar o CD. Aceitável dado o contexto do AWS Academy.
- **Risco de esquecer de atualizar os Secrets** — mitigado pelo comentário no cabeçalho do `cd.yml` e por esta ADR.
- **Múltiplos pontos de entrada** — workflows podem ser chamados de diferentes formas (orquestrador vs manual), exige
  documentação clara.

## Casos de Uso

### 1. Desenvolvimento normal (push de código)

```
git push → CI roda automaticamente → Testes passam ✅
```

### 2. Deploy completo (primeira vez ou após mudança de infra)

```
Actions → CD → Run workflow → Infra + Build + Deploy
```

### 3. Re-deploy após correção de bug (sem rebuild de imagem/infra)

```
Actions → Deploy → Run workflow → Seleciona env/região/cluster → Deploy
```

### 4. Limpeza completa do ambiente

```
Actions → Cleanup → Run workflow → Seleciona environment → Destroy tudo
```

### 5. Debug local

```bash
./scripts/connect-eks.sh
kubectl get pods -n mecanica-lab
kubectl logs -f deployment/lab-mecanica-app -n mecanica-lab
```

