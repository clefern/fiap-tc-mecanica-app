# ADR-032 — Autenticação de cliente via CPF emitida por Lambda Serverless

**Status:** Accepted
**Data:** 2026-05-22
**Fase do projeto:** Fase 3 — Tech Challenge (Grupo 14SOAT)
**Decisores:** Grupo 14SOAT
**Contexto regulatório:** Requisito obrigatório AUTH-002 / AUTH-003 do `requisitos-fase-3.md` (PDF oficial 13SOAT)

## Contexto

A Fase 3 exige que **clientes** se autentiquem por CPF (apenas o documento, sem senha) e que essa emissão de token seja feita por uma **Function Serverless** separada da aplicação principal. O token deve ser consumido pelas APIs protegidas da aplicação Spring Boot rodando no EKS.

Antes desta decisão, o `AuthController` (endpoint `/oauth/token`) atendia 100% das autenticações via OAuth 2.0 password grant — email + senha. Esse fluxo continua válido para perfis internos (admin, atendente, mecânico), mas **não atende o requisito de Fase 3 para clientes**.

A Function Serverless (`fiap-tc-mecanica-lambda`, Node.js 20 + TypeScript) já está implementada e provisionada por Terraform (`lambda/infra/`). Ela:
- Valida CPF (módulo 11)
- Faz lookup do cliente em `clientes` no RDS via `ClienteRepository.findByDocumento(Documento)` (espelho da lógica do app)
- Emite JWT HS256 com `subject = email_do_cliente`, claims `id` (clienteId) e `role: CLIENTE`, assinado com a **mesma `SECURITY_JWT_SECRET_KEY` do K8s Secret do app**

## Decisão

**Opção A — App não muda; JwtAuthenticationFilter aceita JWT da Lambda transparentemente.**

O fluxo de cliente passa a ser:

```
Cliente → Traefik IngressRoute (k8s/base/app-ingressroute.yaml)
   ├── POST /auth → API Gateway AWS → Lambda CPF→JWT → resposta { access_token, ... }
   └── GET/POST /api/* → app Spring Boot
         └── JwtAuthenticationFilter valida o JWT (mesma secret HS256) →
             carrega UserDetails via UserDetailsService.loadUserByUsername(email) →
             SecurityContextHolder autenticado → endpoint executa
```

O `JwtAuthenticationFilter` (`app/src/main/java/com/fiap/mecanica/infra/config/security/JwtAuthenticationFilter.java`) já faz **exatamente** o que precisamos:

1. Extrai `Bearer <token>` do header `Authorization`
2. Chama `jwtService.extractUsername(jwt)` → retorna o `subject` (= email)
3. Carrega `UserDetails` via `userDetailsService.loadUserByUsername(email)`
4. Valida via `jwtService.isTokenValid(token, userDetails)` (assinatura HS256 com a mesma secret + não revogado)
5. Popula `SecurityContextHolder`

Como a Lambda emite com a **mesma secret e o mesmo algoritmo** (HS256), e o `subject` é o email do cliente (existente na tabela `users` por ser também `CLIENTE`), o filter aceita sem nenhuma mudança.

### Consequências

- **`AuthController.getToken` (`/oauth/token`)** fica marcado `@Deprecated` para clientes — continua funcional para admin/atendente/mecânico (perfis internos que não passam pela Lambda).
- **Swagger** documenta o novo fluxo via tag dedicada no `AuthApi` e nota em `getToken`.
- **Nenhum novo controller** no app — reduz superfície de ataque e ponto de falha.
- **Nenhuma duplicação de lógica de validação de CPF**: a Lambda faz, o app só consome o JWT.

## Alternativas consideradas

### Opção B — App cria `/auth/cpf` proxy que delega pra Lambda

App teria um novo endpoint que internamente chama a Lambda via HTTP.

**Por que descartada:** adiciona um hop desnecessário (cliente → app → Lambda em vez de cliente → Lambda direto), aumenta latência, cria ponto de falha extra no app, requer client HTTP, retry policy, etc. O Traefik IngressRoute já roteia `/auth` direto pra Lambda — esse é o desenho cloud-native correto.

### Opção C — Implementar a lógica de CPF→JWT TAMBÉM no app (sem usar Lambda)

App ganharia endpoint `/auth/cpf` que faria lookup do cliente direto no banco e emitiria JWT.

**Por que descartada:** vai contra o requisito formal da Fase 3 ("**Function Serverless** para autenticação"). Não atende a rubrica.

### Opção D — Cognito + JWKS (RS256)

Trocar HS256 simétrico por RS256 com chaves públicas via Cognito ou similar.

**Por que descartada agora:** mudança de algoritmo invasiva no app (JwtService inteiro mudaria), custo extra do Cognito, complexidade desnecessária pro escopo da Fase 3. Fica como ADR futuro se evoluir pra produção real.

## Contrato do JWT da Lambda (validado pelo app sem mudança)

| Campo | Valor | Validado por |
|---|---|---|
| Algorithm | HS256 | `JwtService.extractAllClaims` |
| Secret | `SECURITY_JWT_SECRET_KEY` (mesmo do K8s Secret) | `Keys.hmacShaKeyFor(Base64.decode(secret))` |
| `subject` | email do cliente (string) | `loadUserByUsername` no `UserDetailsService` |
| `id` (claim custom) | UUID do cliente | (ignorado pelo app — opcional) |
| `role` (claim custom) | `"CLIENTE"` | (ignorado pelo app — roles vem do `UserDetails`) |
| `iat` | timestamp emissão | `Jwts.parser` |
| `exp` | timestamp expiração | `Jwts.parser` (rejeita se expirado) |

## Riscos e mitigações

| Risco | Mitigação |
|---|---|
| Secret HS256 vazada compromete app + Lambda | Secret rotacionável; manter em AWS Secrets Manager (não em código). Single source of truth para os 3 stacks (K8s Secret + Terraform Lambda + Terraform app). |
| Cliente sem email cadastrado (`users.email` nulo) → `loadUserByUsername` falha | Migration de seed garante email; cadastro de cliente exige email; Lambda valida que cliente tem email antes de emitir. |
| Token da Lambda nunca expira (claim `exp` ausente) | Lambda **sempre** emite com `ACCESS_TOKEN_TTL_SECONDS` (default 3600). Coberto por teste unitário em `lambda/tests/handler.test.ts`. |
| Cliente perdido no banco (deletado) ainda tem JWT válido | TTL curto (1h); revogação via `RevokedTokenEntity` se necessário (endpoint `/oauth/revoke` continua funcional). |

## Validação

- ✅ **Já existente**: testes unitários do `JwtService` cobrem extração de claims, validação de assinatura, expiração.
- ✅ **Já existente**: testes unitários do `JwtAuthenticationFilter` cobrem fluxo Bearer → SecurityContext.
- ✅ **Adicionado neste PR**: teste de integração `JwtFromExternalIssuerIT` que emite um JWT externo com `subject = email_de_user_existente` e prova que um endpoint protegido aceita (mesma garantia que a Lambda dará).
- ⏳ **Pendente (Onda 3)**: teste E2E real com Lambda deployada em lab — depende do `terraform apply` de `lambda/infra/`.

## Referências

- Requisito oficial: `requisitos-fase-3.md` (AUTH-002, AUTH-003)
- ADR-010: estratégia de autorização híbrida (mantida)
- ADR-031: decisões consolidadas Fase 2
- Handler da Lambda: `fiap-tc-mecanica-lambda/src/handler.ts`
- IngressRoute do Traefik: `fiap-tc-mecanica-infra-k8s/k8s/base/app-ingressroute.yaml`
