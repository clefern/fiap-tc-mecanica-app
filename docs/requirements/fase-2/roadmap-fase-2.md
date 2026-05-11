# Plano Fase 2 — Estratégia Completa de Entrega
## Mecânica API — Tech Challenge FIAP (Grupo 14SOAT)

> **Propósito deste arquivo**: Guia de execução para sessões futuras de IA. Cobre o gap-analysis
> completo entre o código atual e os requisitos de Fase 2, com instruções técnicas detalhadas
> por item, sequência de execução e referências a padrões já existentes no projeto.

---

## Gap Analysis — Estado Atual

> **Última atualização**: 2026-03-19

### ✅ Concluído (acumulado)

| ID | Entregável | Evidência |
|----|-----------|-----------|
| API-001 | POST /api/ordens-servico/abertura-completa | feat/abertura-os-completa → merged |
| API-002 | GET /api/ordens-servico/fila-operacional | feat/fila-operacional → merged |
| API-003 | GET /api/ordens-servico/{id}/status | feat/api-003-status-os → merged |
| SEC-001 | Remover token Sonar hardcoded + revogar | feat/hardcoded-sonar-token |
| INT-001 | POST /api/integracoes/orcamentos/aprovacao (API key M2M) | merged |
| EMAIL-001 | Links HMAC-SHA256 nos emails de orçamento | merged |
| REFACTOR | OrdemServicoServiceImpl decomposto em serviços menores | refactor/os-dependencies |
| SWAGGER | 15 interfaces *Api com docs melhoradas (401/403/422, SecurityRequirement) | refactor/os-dependencies |
| DOCKER | Dockerfile multi-stage (JRE Alpine) + Dockerfile.deploy (distroless) | /Dockerfile, /Dockerfile.deploy |
| COMPOSE | docker-compose.yml com app, postgres, adminer, mailhog, sonarqube | /docker-compose.yml |
| K8S-001 | Manifestos K8s: namespace, configmap, secret, deployments, services, pvc | /k8s/base/ + /k8s/overlays/ |
| K8S-002 | HPA: CPU 70%, Memory 80%, min 1, max 10 réplicas | /k8s/base/hpa.yaml |
| TF-001 | Terraform: VPC, subnets, NAT, IGW, EKS cluster (1.33), node group t3.large | /infra/conf/ |
| TF-002 | Terraform: ECR, ALB Controller, NGINX Ingress, Metrics Server, Cert-Manager | /infra/conf/ |
| CICD-001 | GitHub Actions CI: compilação Maven, cache, testes, cobertura JaCoCo | .github/workflows/ci.yml + test.yml |
| CICD-002 | GitHub Actions Docker build & push para ECR + Trivy scan | .github/workflows/build.yml |
| CICD-003 | GitHub Actions Deploy EKS via kubectl + k8s-deploy.sh (envsubst) | .github/workflows/deploy.yml + cd.yml |
| SEC-K8S | K8s Secrets (DB_USER, DB_PASSWORD, JWT_SECRET_KEY, INTEGRATION_API_KEY) | /k8s/base/secret.yaml |
| QUAL-003 | JWT secret default removido de application.yml; fallback movido para perfil dev | 2026-03-19 |
| QUAL-002 | V16__add_indexes.sql — 3 índices: status, status+data_entrada, orcamentos | 2026-03-19 |
| QUAL-001 | @BatchSize(20) em OrdemServicoEntity.itens (native query — @EntityGraph n/a) | 2026-03-19 |
| DOC-001 | README.md — seção Fase 2 completa com diagrama ASCII de arquitetura AWS | 2026-03-19 |
| ADR-031 | ADR consolidado Fase 2 (ADR-030 já ocupado por outro ADR) | docs/ADRs/ADR-031-decisoes-fase2.md |

### ❌ Pendente — Externo (equipe)

| ID | Item | Prioridade |
|----|------|-----------|
| DOC-002 | Vídeo demonstrativo ≤ 15 min (deploy + CI/CD + APIs + HPA) | P1 — bloqueia entrega |
| DOC-003 | PDF de Entrega (link GitHub + diagrama arquitetura + link vídeo) | P1 — bloqueia entrega |
| INSOMNIA | Verificar se export Insomnia tem endpoints da Fase 2 | P2 |

---

## Sequência de Execução

```
Sessão 1:  SEC-001 + QUAL-003 + API-003              ✅ CONCLUÍDA (2026-03-09/11)
Sessão 2:  INT-001                                   ✅ CONCLUÍDA
Sessão 3:  EMAIL-001                                 ✅ CONCLUÍDA
Sessão 4:  K8S-001 + K8S-002 + TF-001 + TF-002      ✅ CONCLUÍDA
Sessão 5:  CICD-001 + CICD-002 + CICD-003            ✅ CONCLUÍDA
Sessão 6:  REFACTOR + SWAGGER                        ✅ CONCLUÍDA (2026-03-18)
Sessão 7:  QUAL-001 + QUAL-002 + DOC-001 + ADR-031   ✅ CONCLUÍDA (2026-03-19)
Pendente:  DOC-002 (vídeo) + DOC-003 (PDF)           ❌ Externo — equipe
```

---

## Convenção Obrigatória — Insomnia Export

**A cada novo endpoint implementado**, atualizar `docs/api/Insomnia_export.yaml` com **dois** entries:

### 1. Fluxo Completo (E2E) — pasta raiz "Fluxo Completo (E2E) - Fixed Users"
- Numeração sequencial: `"NN. Role - Descrição da ação"` (ex.: `"31. Atendente - Consultar Status da OS"`)
- `sortKey`: próximo inteiro após o último step do fluxo
- Autenticação: `{% response 'body', 'req_<id-do-login>', 'b64::JC5hY2Nlc3NfdG9rZW4=::46b', 'never', 60 %}`
- Usar response chaining para referenciar IDs de steps anteriores quando o endpoint precisar de um ID
- IDs de request: prefixo `req_` + slug descritivo + `_e2e_NNN`

### 2. Pasta temática — Endpoints > Core dominio > \<pasta correta\>
- Nome sem numeração: `"Verbo + Recurso"` (ex.: `"Consultar Status da OS"`)
- Usar variável de ambiente: `{{ access_token }}`, `{{ os_id }}`, `{{ cliente_id }}`, etc.
- `sortKey`: valor que mantenha ordenação lógica dentro da pasta (GET list < GET by id < POST < PUT < DELETE)
- IDs de request: prefixo `req_` + slug descritivo + `_core_NNN`

### Pasta de destino por recurso
| Recurso | Pasta no Insomnia |
|---------|------------------|
| `/api/ordens-servico/**` | Endpoints > Core dominio > Ordens de Serviço |
| `/api/orcamentos/**` | Endpoints > Core dominio > Orçamentos |
| `/api/integracoes/**` | Endpoints > Core dominio > Integrações *(criar se não existir)* |
| `/api/clientes/**` | Endpoints > Core dominio > Clientes |
| `/api/mecanicos/**` | Endpoints > Core dominio > Mecânicos |
| `/api/estoque/**` | Endpoints > Estoque |

---

## Trilha 1 — APIs Java/Spring Boot

---

### API-003 — GET /api/ordens-servico/{id}/status

**Contexto**: O requisito pede um endpoint dedicado de status da OS com resposta enxuta
(status atual + timestamps relevantes). Hoje só existe `GET /api/ordens-servico/{id}` que
retorna o recurso completo. Criar endpoint dedicado permite consumo leve (ex.: polling de
status por frontend sem trafegar dados de itens).

**Arquivos a criar/modificar**:

```
CRIAR:
  src/main/java/com/fiap/mecanica/presentation/dto/StatusOsResponse.java

MODIFICAR:
  src/main/java/com/fiap/mecanica/presentation/api/OrdemServicoControllerApi.java
  src/main/java/com/fiap/mecanica/presentation/controller/OrdemServicoController.java
  src/main/java/com/fiap/mecanica/presentation/mapper/OrdemServicoMapper.java

TESTES:
  src/test/java/com/fiap/mecanica/presentation/controller/OrdemServicoControllerTest.java
    (adicionar método deveRetornarStatusDaOs)
```

**DTO a criar** — `StatusOsResponse.java`:
```java
public record StatusOsResponse(
    UUID id,
    String codigo,
    StatusOS status,
    String statusDescricao,   // ex.: "Em Execução"
    LocalDateTime dataEntrada,
    LocalDateTime dataAprovacao,  // null se não aprovada ainda
    LocalDateTime dataFechamento  // null se não finalizada
) {}
```

**Padrão a reutilizar**:
- Ver `OrdemServicoResponse.java` para estrutura de DTO existente
- Ver `buscarPorId` no controller (linha ~93) como modelo do endpoint
- Ver `OrdemServicoMapper.toResponse()` — criar `toStatusResponse()` análogo

**Endpoint**:
```java
// Em OrdemServicoController.java — após buscarPorId
@Override
@GetMapping("/{id}/status")
@PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN', 'CLIENTE')")
public ResponseEntity<StatusOsResponse> buscarStatus(@PathVariable UUID id) {
  OrdemServico os = service.buscarPorId(id);
  return ResponseEntity.ok(mapper.toStatusResponse(os));
}
```

**Nota de segurança**: Permitir CLIENTE consultar status da própria OS. Considerar adicionar
`@osSecurity.canView(authentication, #id)` se quiser granularidade por cliente.

**Testes**:
- Controller test: mock `service.buscarPorId`, verificar `$.status`, `$.codigo`
- Testar 404 quando OS não existe

**DoD**: Endpoint retorna 200 com DTO enxuto. Swagger documenta. Insomnia atualizado.

---

### INT-001 — POST /api/integracoes/orcamentos/aprovacao

**Contexto**: O requisito pede endpoint para "notificações externas de aprovação/recusa do
orçamento do cliente". Hoje existe `POST /api/orcamentos/os/{osId}/aprovar` mas requer
autenticação JWT com role CLIENTE/ADMIN, não adequado para integração machine-to-machine.
Criar controller dedicado com autenticação por API key (via header `X-Api-Key`).

**Arquivos a criar/modificar**:

```
CRIAR:
  src/main/java/com/fiap/mecanica/presentation/dto/AprovacaoOrcamentoExternaRequest.java
  src/main/java/com/fiap/mecanica/presentation/api/IntegracaoOrcamentoControllerApi.java
  src/main/java/com/fiap/mecanica/presentation/controller/IntegracaoOrcamentoController.java
  src/main/java/com/fiap/mecanica/infra/config/security/ApiKeyAuthFilter.java
  src/test/java/.../controller/IntegracaoOrcamentoControllerTest.java

MODIFICAR:
  src/main/java/com/fiap/mecanica/infra/config/security/SecurityConfig.java
    (adicionar rota /api/integracoes/** na chain com filtro de API key)
  src/main/resources/application.yml
    (adicionar mecanica.integrations.api-key: ${INTEGRATION_API_KEY:dev-key-local})
```

**DTO de entrada**:
```java
public record AprovacaoOrcamentoExternaRequest(
    @NotBlank String osCodigo,     // código da OS (ex: "OS-2025-001")
    @NotNull DecisaoOrcamento decisao  // enum: APROVADO, REPROVADO
) {}
```

**Endpoint**:
```java
// POST /api/integracoes/orcamentos/aprovacao
// Header: X-Api-Key: {valor configurado em application.yml}
```

**Fluxo interno**: Chamar `OrcamentoService.aprovarPorOsId(osId)` ou
`OrcamentoService.reprovarPorOsId(osId)` — já existem em
`src/main/java/com/fiap/mecanica/application/service/impl/OrcamentoServiceImpl.java` (linhas ~195-208).

**Busca por código**: Necessário adicionar `findByCodigo(String codigo)` em
`OrdemServicoRepository` (domain port) e `JpaOrdemServicoRepository` (Spring Data — método
derivado `findByCodigo` funciona automaticamente).

**Autenticação via API Key**:
```java
// ApiKeyAuthFilter.java — Servlet filter
// Lê header X-Api-Key, compara com @Value("${mecanica.integrations.api-key}")
// Se válido: seta SecurityContextHolder com Authentication simples (ROLE_INTEGRATION)
// Se inválido: retorna 401
```

**SecurityConfig**: A rota `/api/integracoes/**` deve usar o filtro de API key em vez de JWT.
Adicionar `.addFilterBefore(apiKeyFilter, JwtAuthenticationFilter.class)` para essa rota.

**Para K8s**: A API key será injetada via Secret (`INTEGRATION_API_KEY`).

**Testes**:
- Com header correto + OS existente → 200
- Com header errado → 401
- Com OS não encontrada → 404
- Com status de OS inválido para aprovação → 422

**DoD**: Endpoint funciona com API key. Sem JWT necessário. Testado. Documentado no Swagger
com nota "Autenticação via X-Api-Key header".

---

### EMAIL-001 — Links de Ação com Token no Email de Orçamento

**Contexto**: O requisito pede "atualização de status da OS via ferramenta como email".
Confirmado com o professor: as **ações externas via email se restringem exclusivamente à
aprovação ou reprovação do orçamento pelo cliente**. Emails de OS criada e OS finalizada
permanecem puramente informativos.

O email de orçamento gerado (`orcamento-gerado.html`) já existe mas o link de aprovação
não tem segurança de token — qualquer pessoa com o link poderia aprovar. A solução é
**adicionar token HMAC-SHA256 de curta duração** nos links de aprovar/reprovar do email
de orçamento, permitindo que o cliente execute a ação sem precisar estar logado via JWT.

O endpoint de integração externa `POST /api/integracoes/orcamentos/aprovacao` (INT-001)
já suporta a ação de aprovação/reprovação via API key. EMAIL-001 adiciona a **camada de
token por link** que permite ao cliente agir diretamente pelo email.

**Arquivos a criar/modificar**:

```
CRIAR:
  src/main/java/com/fiap/mecanica/infra/security/ActionTokenService.java
    (gera e valida tokens HMAC-SHA256 com expiração — escopo: aprovação de orçamento)

MODIFICAR:
  src/main/java/com/fiap/mecanica/application/service/NotificacaoEmailApplicationService.java
    (ao enviar email de orçamento gerado, gerar dois links tokenizados: aprovar e reprovar)
  src/main/resources/templates/email/orcamento-gerado.html
    (substituir links estáticos por links com token — botões "Aprovar Orçamento" e "Recusar Orçamento")
  src/main/java/com/fiap/mecanica/presentation/controller/IntegracaoOrcamentoController.java
    (adicionar parâmetro opcional ?token= como alternativa à X-Api-Key para chamadas via link de email)
  src/main/resources/application.yml
    (mecanica.mail.action-token-secret: ${ACTION_TOKEN_SECRET:dev-secret})
    (mecanica.mail.action-token-expiry-minutes: 1440)
```

**ActionTokenService** — comportamento:
```java
// gerar: HMAC-SHA256(secret, "orcamentoId:APROVADO:expiryEpoch") → Base64URL
// validar: recomputa e compara + verifica expiração
String generate(UUID orcamentoId, String decisao, int minutesToExpire);
boolean validate(UUID orcamentoId, String decisao, String token);
```

**Template `orcamento-gerado.html`** — substituir links atuais por links tokenizados:
```html
<!-- Dois botões gerados dinamicamente pelo NotificacaoEmailApplicationService -->
<a th:href="${linkAprovar}">Aprovar Orçamento</a>
<a th:href="${linkRecusar}">Recusar Orçamento</a>
<!-- Exemplo de URL gerada: /api/integracoes/orcamentos/aprovacao?token=<hmac>&decisao=APROVADO -->
```

**Endpoint de integração** (em IntegracaoOrcamentoController) — aceitar token de email
como alternativa à X-Api-Key:
```java
// GET /api/integracoes/orcamentos/aprovacao?token=<hmac>&decisao=APROVADO|REPROVADO
// Sem necessidade de header X-Api-Key quando token presente e válido
// Token válido → executar aprovação/reprovação; Token inválido/expirado → 401
```

Usar GET (e não POST) para o fluxo de email, pois o cliente clica em um link no navegador.
O endpoint POST existente (INT-001) permanece para integração machine-to-machine com X-Api-Key.

**Nota**: Para o escopo do Tech Challenge, o token HMAC sem refresh é suficiente.
A demonstração em vídeo mostrará o email chegando no MailHog, o cliente clicando em
"Aprovar Orçamento" e o status do orçamento mudando para APROVADO.

**DoD**: Email de orçamento gerado contém links tokenizados de aprovar e recusar. Clique
no link transita o status do orçamento sem necessidade de login JWT. Testado com
Thymeleaf + MailHog em ambiente dev.

---

## Trilha 2 — Infraestrutura

---

### K8S-001 + K8S-002 — Manifestos Kubernetes

**Criar estrutura `/k8s/`**:

```
k8s/
  namespace.yaml
  app-deployment.yaml      ← inclui liveness/readiness probes + resources
  app-service.yaml         ← LoadBalancer ou NodePort
  app-configmap.yaml       ← vars não sensíveis
  app-secret.yaml          ← DB, JWT, API key (base64, NÃO commitar valores reais)
  postgres-statefulset.yaml  ← para ambiente sem RDS
  postgres-service.yaml
  app-hpa.yaml             ← CPU target 60%, min 1, max 5
```

**app-deployment.yaml** — pontos críticos:
```yaml
containers:
  - name: mecanica-api
    image: ${ECR_REGISTRY}/mecanica-api:${IMAGE_TAG}
    resources:
      requests: { cpu: "250m", memory: "512Mi" }
      limits:   { cpu: "1000m", memory: "1Gi" }
    readinessProbe:
      httpGet: { path: /actuator/health/readiness, port: 8080 }
      initialDelaySeconds: 30
    livenessProbe:
      httpGet: { path: /actuator/health/liveness, port: 8080 }
      initialDelaySeconds: 60
    env:
      - name: SPRING_DATASOURCE_URL
        valueFrom: { secretKeyRef: { name: mecanica-secrets, key: db-url } }
      - name: JWT_SECRET
        valueFrom: { secretKeyRef: { name: mecanica-secrets, key: jwt-secret } }
      - name: INTEGRATION_API_KEY
        valueFrom: { secretKeyRef: { name: mecanica-secrets, key: integration-api-key } }
```

**Actuator**: Verificar que `application.yml` expõe `liveness` e `readiness`:
```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

**app-hpa.yaml** (K8S-002):
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  minReplicas: 1
  maxReplicas: 5
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 60
```

**DoD**: `kubectl apply -f k8s/` sobe app saudável. HPA observável com `kubectl get hpa`.

---

### TF-001 + TF-002 — Terraform (AWS Academy)

**Criar estrutura `/infra/`**:

```
infra/
  main.tf          ← provider aws, terraform backend
  variables.tf     ← region, cluster_name, db_name, instance_type
  outputs.tf       ← cluster_endpoint, rds_endpoint, rds_port
  modules/
    vpc/           ← VPC + subnets públicas/privadas + IGW + NAT
    eks/           ← cluster EKS + node group
    rds/           ← RDS PostgreSQL 16
  README.md        ← instruções terraform init/plan/apply
```

**Variáveis obrigatórias** (`variables.tf`):
```hcl
variable "aws_region"     { default = "us-east-1" }
variable "cluster_name"   { default = "mecanica-cluster" }
variable "db_username"    { sensitive = true }
variable "db_password"    { sensitive = true }
variable "db_name"        { default = "mecanica" }
```

**Outputs críticos** (`outputs.tf`):
```hcl
output "rds_endpoint" { value = module.rds.endpoint }
output "eks_cluster_endpoint" { value = module.eks.cluster_endpoint }
output "kubeconfig_command" {
  value = "aws eks update-kubeconfig --region ${var.aws_region} --name ${var.cluster_name}"
}
```

**Fluxo de uso** (documentar no README):
```bash
cd infra/
terraform init
terraform plan -var="db_username=..." -var="db_password=..."
terraform apply
# Após apply: exportar outputs para criar K8s secret com valores reais
```

**DoD**: Cluster EKS + RDS criados via Terraform. `kubectl get nodes` retorna nodes saudáveis.
App conecta ao RDS (verificar via logs de startup do Spring).

---

### CICD-001 + CICD-002 + CICD-003 — GitHub Actions

**Criar `.github/workflows/ci-cd.yml`**:

```yaml
# Estrutura de 3 jobs sequenciais:
#
# 1. build-and-test (CI)
#    Trigger: push em qualquer branch, PRs para main/develop
#    Steps: checkout → Java 21 → cache Maven → ./mvnw verify → spotless:check
#
# 2. docker-build-push (CD - imagem)
#    Trigger: apenas push em main
#    Needs: build-and-test
#    Steps: checkout → AWS credentials → login ECR → docker build → push (SHA + latest)
#
# 3. deploy (CD - cluster)
#    Trigger: apenas push em main
#    Needs: docker-build-push
#    Steps: checkout → AWS credentials → kubeconfig → envsubst nos YAMLs → kubectl apply
#           → kubectl rollout status deployment/mecanica-api
```

**Secrets necessários no GitHub** (configurar em Settings > Secrets):
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN       ← necessário para AWS Academy
ECR_REGISTRY            ← <account_id>.dkr.ecr.us-east-1.amazonaws.com
ECR_REPOSITORY          ← mecanica-api
EKS_CLUSTER_NAME        ← mecanica-cluster
AWS_REGION              ← us-east-1
DB_URL                  ← jdbc:postgresql://rds-endpoint:5432/mecanica
DB_USERNAME
DB_PASSWORD
JWT_SECRET
INTEGRATION_API_KEY
```

**Substituição de variáveis nos manifests**:
No job de deploy, antes do `kubectl apply`, substituir `${IMAGE_TAG}` nos YAMLs:
```bash
IMAGE_TAG="${GITHUB_SHA:0:7}"
sed -i "s|\${ECR_REGISTRY}/mecanica-api:\${IMAGE_TAG}|$ECR_REGISTRY/mecanica-api:$IMAGE_TAG|g" k8s/app-deployment.yaml
```

**DoD**: Commit em `main` dispara pipeline completo. App nova versão rodando em K8s em < 5 min.
CI falha em PRs com testes quebrados.

---

## Trilha 3 — Segurança e Qualidade Crítica

---

### SEC-001 — Revogar token Sonar exposto

**Arquivo com problema**: `scripts/sonarQubeDocker.txt`

**Ação**:
1. Revogar o token no servidor Sonar local (interface `http://localhost:9000`)
2. Remover o valor hardcoded do arquivo, substituir por placeholder:
   `sonar.login=${SONAR_TOKEN}`
3. Criar `sonar-project.properties` na raiz com configuração do projeto
4. No CI (CICD-001), usar secret `SONAR_TOKEN` no step de qualidade

---

### QUAL-003 — JWT secret não deve ter valor default

**Arquivo**: `src/main/resources/application.yml` linha ~38

**Problema atual**:
```yaml
security:
  jwt:
    secret-key: 404E635266556A586E327235... # valor fixo hardcoded
```

**Correção**:
```yaml
security:
  jwt:
    secret-key: ${JWT_SECRET}  # obrigatório via env var — sem default
```

**Impacto**: Em dev local, precisará setar `JWT_SECRET` no `.env` ou no
`application-dev.yml` (não commitado). O `application-test.yml` já tem valor fixo e
está correto para testes.

---

### QUAL-001 — N+1 query em findAll de OrdemServico

**Arquivo**: `src/main/java/com/fiap/mecanica/infra/jpa/JpaOrdemServicoRepository.java`

**Problema**: `findAll(pageable)` carrega OS sem itens, depois carrega itens individualmente
por lazy loading quando o mapper os acessa.

**Correção**: Adicionar `@EntityGraph` na listagem principal ou usar JPQL com JOIN FETCH:
```java
@EntityGraph(attributePaths = {"itens"})
Page<OrdemServicoEntity> findAll(Pageable pageable);
// ou manter o findAll default e criar uma versão com join para a fila operacional
```

**Alternativa mais segura**: Para listagens paginadas grandes (fila-operacional), a
`OrdemServicoResponse` atual inclui itens? Se sim, adicionar EntityGraph. Se a
`StatusOsResponse` não precisa de itens, o problema não se aplica à API-003.

---

### QUAL-002 — Índices faltando em BD

**Criar nova migration**: `src/main/resources/db/migration/V16__add_performance_indexes.sql`

```sql
-- Índice principal para filtros de status (fila-operacional, listagens)
CREATE INDEX IF NOT EXISTS idx_ordens_servico_status
  ON public.ordens_servico(status);

-- Índice composto para buscas de orçamento por OS + status
CREATE INDEX IF NOT EXISTS idx_orcamentos_os_status
  ON public.orcamentos(ordem_servico_id, status);

-- Índice para busca por código (INT-001 usa código para localizar OS)
CREATE INDEX IF NOT EXISTS idx_ordens_servico_codigo
  ON public.ordens_servico(codigo);
```

---

## Documentação

### DOC-001 — README.md — Seção Fase 2

Adicionar ao README existente:

```markdown
## Fase 2 — Infraestrutura, Escalabilidade e Deploy

### Objetivos da Fase 2
[resumo dos requisitos]

### Arquitetura de Infraestrutura
[diagrama ou link para docs/arquitetura]
- App: EKS (Kubernetes) com HPA
- Banco: RDS PostgreSQL 16
- Registry: ECR
- CI/CD: GitHub Actions

### Deploy em Kubernetes
[instruções kubectl]

### Provisionamento com Terraform
[instruções terraform init/plan/apply]

### Novas APIs — Fase 2
| Endpoint | Descrição |
|----------|-----------|
| POST /api/ordens-servico/abertura-completa | ✅ |
| GET  /api/ordens-servico/fila-operacional  | ✅ |
| GET  /api/ordens-servico/{id}/status       | 🔲 |
| POST /api/integracoes/orcamentos/aprovacao | 🔲 |
```

### ADR-030 — Decisões Arquiteturais da Fase 2

Criar `docs/ADRs/ADR-030-decisoes-fase-2.md` cobrindo:
- Decisão de não usar CQRS/microsserviços (manter monólito hexagonal)
- Escolha de API key para integração externa (vs OAuth2 machine-to-machine)
- Escolha de links com HMAC token no email de orçamento para aprovação/reprovação pelo cliente (escopo restrito: apenas orçamento, não outras transições de status)
- Terraform + EKS + RDS como IaC strategy
- GitHub Actions como CI/CD (vs GitLab CI)

---

## Checklist de Entregáveis (Fase 2 completa)

- [x] Código refatorado (hexagonal, clean code) — OrdemServicoServiceImpl decomposto
- [x] Abertura de OS com cliente + veículo + itens — API-001 ✅
- [x] Consulta de status da OS — API-003 ✅
- [x] Aprovação de orçamento via notificação externa — INT-001 ✅
- [x] Listagem operacional filtrada e ordenada — API-002 ✅
- [x] Atualização de status via email — EMAIL-001 ✅
- [x] Testes automatizados (1052+ testes passando) ✅
- [x] Dockerfile + docker-compose ✅
- [x] Manifestos Kubernetes em /k8s — K8S-001/002 ✅
- [x] Scripts Terraform em /infra — TF-001/002 ✅
- [x] Pipeline CI/CD — CICD-001/002/003 ✅
- [x] README atualizado com seção Fase 2 + diagrama ASCII — DOC-001 ✅
- [x] ADR consolidado Fase 2 — ADR-031 ✅ (ADR-030 já ocupado)
- [ ] Link para collection de APIs (Insomnia) — verificar se export está atualizado
- [ ] Vídeo demonstrativo (YouTube/Vimeo, até 15 min) — DOC-002 ❌ externo
- [ ] PDF de Entrega — DOC-003 ❌ externo

---

## Comparação com Requisitos Oficiais

**Score: 20/22 requisitos atendidos no código. Faltam 2 externos (vídeo + PDF).**

| Requisito Oficial | Status |
|---|---|
| Abertura de OS (cliente + veículo + serviços + peças em um payload) | ✅ |
| Consulta de Status (situação atual da OS) | ✅ |
| Aprovação de Orçamento via notificação externa | ✅ |
| Listagem com ordenação (execução > aguardando > diagnóstico > recebida) | ✅ |
| Atualização de Status via Email (links de ação com token HMAC) | ✅ |
| Testes automatizados cobrindo fluxos críticos | ✅ |
| Dockerfile atualizado | ✅ |
| docker-compose para desenvolvimento local | ✅ |
| Manifestos K8s: Deployments, Services, ConfigMaps, Secrets | ✅ |
| HPA escalando por CPU/memória | ✅ |
| IaC Terraform — Cluster Kubernetes | ✅ |
| IaC Terraform — Banco de dados | ⚠️ K8s Deployment (não RDS gerenciado) |
| CI/CD — Build da aplicação | ✅ |
| CI/CD — Execução de testes | ✅ |
| CI/CD — Build da imagem Docker | ✅ |
| CI/CD — Deploy no Kubernetes | ✅ |
| CI/CD — Deploy do banco de dados | ✅ (K8s postgres) |
| CI/CD — Aplicação dos manifestos | ✅ |
| README.md com descrição, arquitetura e instruções | ✅ |
| Desenho de arquitetura (diagrama ASCII no README) | ✅ |
| Link vídeo demonstrativo (até 15 min) | ❌ externo — DOC-002 |
| PDF de Entrega | ❌ externo — DOC-003 |

**Nota sobre TF-002 (RDS):** O banco PostgreSQL roda como Deployment K8s (com PVC gp2 no EKS),
não como AWS RDS. Atende funcionalmente ao requisito de "banco de dados" via IaC, mas
não é RDS gerenciado. Se a banca exigir RDS dedicado, há um gap aqui.
