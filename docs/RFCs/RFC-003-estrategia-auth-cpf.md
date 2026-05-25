# RFC-003 — Estratégia de autenticação de cliente por CPF

**Status:** Accepted (ver [ADR-032](../ADRs/ADR-032-autenticacao-cpf-via-lambda.md))
**Data:** 2026-05-22
**Autores:** Grupo 14SOAT

## Resumo

Fase 3 exige autenticação de **clientes** por CPF via **Function Serverless**. Quatro estratégias avaliadas; escolhida a **Opção A: Lambda emite JWT HS256 com mesma secret do app, validado transparentemente por `JwtAuthenticationFilter` sem mudança**.

## Contexto

- App Spring Boot já valida JWT HS256 via `JwtAuthenticationFilter` + `JwtService` (`SECURITY_JWT_SECRET_KEY`)
- Tabela `users` tem clientes com `email` único (necessário pro `loadUserByUsername`)
- Lambda Node.js TypeScript já implementada: valida CPF (módulo 11), busca cliente no RDS, emite JWT
- Traefik IngressRoute em `infra-k8s/k8s/base/app-ingressroute.yaml` já roteia `/auth → API Gateway → Lambda`

## Critérios

| Critério | Peso |
|---|---|
| Mínima mudança no código do app | ⭐⭐⭐⭐⭐ |
| Conformidade com requisito Fase 3 ("Function Serverless") | ⭐⭐⭐⭐⭐ |
| Segurança (secret single-source-of-truth) | ⭐⭐⭐⭐⭐ |
| Latência cliente → JWT → endpoint protegido | ⭐⭐⭐⭐ |
| Manutenibilidade futura (rotação de chave, troca de algoritmo) | ⭐⭐⭐ |

## Alternativas

### Opção A — Lambda emite JWT HS256 com mesma secret (ESCOLHIDA)

**Fluxo:** Cliente → Traefik `/auth` → Lambda → JWT → Cliente usa JWT em `/api/*` → `JwtAuthenticationFilter` aceita.

**Prós:**
- **Zero novo código no app** — `JwtAuthenticationFilter` já valida transparentemente
- Cliente faz 1 hop até Lambda (sem passar pelo app)
- Secret única compartilhada via AWS Secrets Manager (planejado) ou injeção em ambos K8s Secret + Terraform variable
- Tags `id`, `role: CLIENTE` no JWT pra logs/audit

**Contras:**
- HS256 simétrico exige que secret seja compartilhada (qualquer leak compromete app + Lambda)
- Rotação exige redeploy coordenado (app + Lambda)

### Opção B — App expõe `/auth/cpf` que faz proxy pra Lambda

**Fluxo:** Cliente → app `/auth/cpf` → Lambda → JWT → app responde JWT pro cliente → cliente usa JWT.

**Prós:**
- Logging centralizado no app
- App pode adicionar lógica extra (rate-limit por IP, fingerprint)

**Contras:**
- **Hop extra** (cliente → app → Lambda em vez de cliente → Lambda direto)
- Latência maior
- App vira ponto de falha extra (timeout, retry, circuit breaker)
- HTTP client no app, mais código pra manter e testar
- Não tira proveito do IngressRoute Traefik que já existe

### Opção C — App implementa lógica CPF→JWT inline (sem usar Lambda)

**Fluxo:** Cliente → app `/auth/cpf` → app valida CPF + busca cliente + emite JWT.

**Prós:** Latência mínima; nenhum serviço externo na rota crítica

**Contras:** **Viola requisito formal da Fase 3** ("Function Serverless para autenticação"). Descartada por bloqueio regulatório.

### Opção D — Cognito + JWKS RS256

**Fluxo:** Cliente → AWS Cognito → ID/Access Token (RS256) → app valida com JWKS público da Cognito.

**Prós:**
- Chave privada nunca sai da AWS
- Rotação de chave automática (Cognito)
- Padrão de mercado pra B2C/B2B

**Contras:**
- Reescrita do `JwtService` (HS256 → RS256 com JWKS resolver)
- Custo: Cognito cobra acima de 50k MAU
- Cognito não tem fluxo nativo de "CPF→token sem senha" — precisa Lambda trigger igual à Opção A
- Mudança grande pra pouco ganho no escopo Fase 3

## Decisão

**Opção A**, registrada em [ADR-032](../ADRs/ADR-032-autenticacao-cpf-via-lambda.md).

Implementação:
- App: marca `AuthController.getToken` como `@Deprecated` para clientes (PR feat/auth-cpf-via-lambda); Swagger atualizado.
- Lambda: já implementada com JWT HS256 + mesma secret.
- Traefik IngressRoute: rota `/auth → Lambda API Gateway` ativa.
- Teste de integração `JwtFromExternalIssuerIT` no app prova que JWT emitido externamente é aceito.

## Riscos & mitigações

| Risco | Mitigação |
|---|---|
| Secret HS256 leak | Single source of truth em AWS Secrets Manager (próxima iteração); auditoria de acesso |
| Token sem `exp` aceito | Lambda sempre seta `exp = now + 3600s`; teste unitário em `lambda/tests/handler.test.ts` |
| Cliente deletado mantém JWT válido | TTL curto (1h); revogação via `RevokedTokenEntity` (endpoint `/oauth/revoke`) |
| Cliente sem email cadastrado | Migration garante email obrigatório; Lambda valida antes de emitir |
