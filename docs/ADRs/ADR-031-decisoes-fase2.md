# ADR-031: Decisões Técnicas Consolidadas — Fase 2

## Status
Aceito

## Contexto

A Fase 2 do Tech Challenge exigiu a evolução da aplicação para garantir qualidade, resiliência e
escalabilidade. Este ADR consolida as principais decisões técnicas tomadas durante essa fase,
complementando os ADRs individuais já existentes.

---

## Decisões

### 1. Kubernetes em EKS (AWS) em vez de ECS ou instâncias EC2

**Decisão**: Usar Amazon EKS com Kustomize (base + overlays por ambiente).

**Razões**:
- Requisito explícito dos avaliadores por Kubernetes
- Kustomize permite reutilizar manifestos base e customizar por ambiente (dev/lab/prod)
  sem duplicação
- HPA nativo no K8s para escalabilidade automática por CPU/memória

**Trade-off aceito**: EKS tem custo mais alto que ECS, mas era requisito avaliativo.

---

### 2. PostgreSQL como Deployment K8s (PVC gp2) em vez de RDS

**Decisão**: Banco de dados rodando como Deployment Kubernetes com PVC EBS (gp2).

**Razões**:
- Ambiente AWS Academy tem restrições de acesso a serviços gerenciados como RDS
- PVC EBS com gp2 oferece persistência suficiente para o escopo do projeto
- Terraform provisiona o banco via Kubernetes provider, mantendo tudo como IaC

**Limitação conhecida**: Em produção real, RDS gerenciado seria preferível (backups
automáticos, multi-AZ, menor overhead operacional). Documentado como débito técnico.

---

### 3. HMAC-SHA256 para links de ação em email de orçamento

**Decisão**: Usar tokens HMAC-SHA256 com expiração (TTL configurável) nos links enviados
por email para aprovação/reprovação de orçamento pelo cliente.

**Razões**:
- Evita exposição de credenciais no link
- TTL protege contra replay attacks
- Sem necessidade de sessão ou autenticação do cliente para a ação

**Implementação**: `ActionTokenService` + `IntegracaoOrcamentoController`.
ADR relacionado: ver ADR-022 (inversão do fluxo de aprovação).

---

### 4. API Key M2M para aprovação de orçamento via integração externa

**Decisão**: Endpoint `POST /api/integracoes/orcamentos/aprovacao` protegido por API Key
no header `X-API-Key`, sem JWT.

**Razões**:
- Integrações máquina-a-máquina (M2M) não seguem o fluxo de login com JWT
- API Key rotacionável via K8s Secret (`INTEGRATION_API_KEY`)
- Separação clara entre endpoints de usuário (JWT) e de integração (API Key)

---

### 5. Decomposição de OrdemServicoServiceImpl

**Decisão**: Dividir `OrdemServicoServiceImpl` (2000+ linhas) em serviços menores:
`OsLifecycleService`, `OsItemService`, `OsQueryService`, mais validators dedicados.

**Razões**:
- Single Responsibility Principle — a classe original acumulou responsabilidades de ciclo
  de vida, itens, consultas e validações
- Facilita testes unitários isolados por responsabilidade
- Melhora legibilidade e manutenibilidade

---

### 6. JWT secret obrigatório via variável de ambiente

**Decisão**: Remover o valor default hardcoded do `SECURITY_JWT_SECRET_KEY` do
`application.yml` base. O perfil `dev` mantém um fallback explicitamente marcado como
não-produção.

**Razões**:
- Um secret hardcoded no repositório pode ser reutilizado em produção inadvertidamente
- A falha explícita no startup (sem env var em prod) é preferível a segurança falsa
- Segue o princípio de fail-fast para configurações críticas de segurança

---

### 7. @BatchSize(20) para itens de OrdemServico

**Decisão**: Usar `@BatchSize(size = 20)` na coleção `itens` de `OrdemServicoEntity`
em vez de `@EntityGraph` nos métodos de listagem.

**Razões**:
- A query de fila operacional (`listarFilaOperacional`) usa `nativeQuery = true`,
  o que impede o uso de `@EntityGraph` diretamente
- `@BatchSize` resolve o N+1 sem alterar as queries: Hibernate agrupa os SELECTs de
  itens em lotes de até 20, reduzindo N queries para ceil(N/20)
- Solução não-intrusiva — funciona para todas as queries que acessam a coleção

---

## Consequências

- Arquitetura da aplicação mais alinhada com princípios SOLID e Hexagonal
- Infraestrutura 100% como código (Terraform + K8s manifests)
- CI/CD automatizado do commit ao deploy em EKS
- Performance de listagem de OS melhorada com batch loading e índices de banco
- Segredos sensíveis não mais hardcoded no repositório
