# Roteiro do vídeo demonstrativo — Fase 3 (Tech Challenge 13SOAT)

> **Duração-alvo**: 15 minutos · **Formato**: tela compartilhada com voz · **Plataforma sugerida**: YouTube (não-listado) ou Vimeo

## Como usar este roteiro

Cada bloco abaixo tem:
- ⏱️ **Tempo** (inicio → fim, total do bloco)
- 🎯 **Objetivo** — o que o avaliador precisa entender ao final desse bloco
- 🗣️ **Fala-base** — texto sugerido; pode ser adaptado. **palavras-chave em negrito** = não esquecer
- 🖥️ **Tela compartilhada** — URL ou caminho local exato (cole no browser/IDE antes de começar a falar)
- 👉 **Cliques / destaques** — o que mostrar com cursor, scroll, zoom ou rectangle highlight
- ⌨️ **Comandos** (quando aplicável) — colar no terminal/curl/etc.
- ⚠️ **Pontos de atenção** — o que NÃO fazer, fallbacks, perguntas-trap

## Preparação antes de gravar (checklist)

- [ ] Browser com **3 abas pré-abertas** (na ordem):
  1. `https://github.com/clefern/fiap-tc-mecanica-app` (logado)
  2. `https://github.com/clefern/fiap-tc-mecanica-infra-k8s/actions/workflows/cd.yml` (página do Provision & Deploy)
  3. `https://one.newrelic.com/` (logado, dashboard "Mecânica" aberto na visão geral)
- [ ] Terminal/iTerm aberto em `~/Workspace/fiap-tc-mecanica/` com fonte ≥ 16pt
- [ ] VS Code aberto no workspace com a árvore expandida em:
  - `app/docs/ADRs/` (visíveis ADR-032..035)
  - `app/docs/RFCs/` (visíveis 001-003)
  - `app/docs/arquitetura/diagramas.md`
- [ ] App Spring Boot rodando local (Docker Compose) com 1 cliente de teste seedado (`CPF 529.982.247-25`)
- [ ] Postman/Insomnia com 2 requisições prontas:
  - `POST http://localhost:8080/auth` body `{"cpf":"529.982.247-25"}`
  - `GET http://localhost:8080/api/clientes/documento/529.982.247-25` com header `Authorization: Bearer {{access_token}}`
- [ ] Câmera **desligada** (vídeo de tela apenas economiza tempo)
- [ ] Notificações de Slack/Discord/Mail silenciadas
- [ ] PDF `entrega-fase3-grupo14soat.pdf` em uma 4ª aba (vai usar no fechamento)

---

## ⏱️ Bloco 1 — Abertura (0:00 → 0:45 · 45s)

### 🎯 Objetivo
Avaliador entende **quem fala**, **o que é o produto**, e o **escopo formal da Fase 3**.

### 🗣️ Fala-base
> "Olá, somos o **Grupo 14SOAT** da pós-graduação em Arquitetura de Software da FIAP. Eu sou **[nome do apresentador]**. Este vídeo apresenta a **Fase 3 do Tech Challenge da Mecânica API** — o sistema de gestão de oficina mecânica que viemos evoluindo desde a Fase 1.
>
> O foco da Fase 3 é elevar a aplicação a um nível de **operação corporativa**: separar tudo em **4 repositórios independentes**, autenticar clientes por **CPF via Function Serverless**, usar **banco gerenciado**, **API Gateway profissional** e **observabilidade ponta a ponta**. Vou mostrar isso nos próximos 15 minutos."

### 🖥️ Tela compartilhada
PDF de entrega aberto na **página 1 (capa)** — `entrega-fase3-grupo14soat.pdf`

### 👉 Cliques / destaques
- Mostrar a **capa do PDF** por 5-8 segundos enquanto fala o nome do grupo
- Scroll suave até os **chips da stack** (4 Repos · Lambda · RDS · Traefik · OTel+NR)

### ⚠️ Atenção
- Não falar mais de 45s na abertura — avaliador quer ver código/sistema rodando
- **Não** explicar Fase 1 e 2 em detalhe — só uma menção rápida

---

## ⏱️ Bloco 2 — Arquitetura: 4 Repositórios + Topologia Cloud (0:45 → 2:45 · 2min)

### 🎯 Objetivo
Avaliador visualiza a **separação dos 4 repos** e a **topologia AWS** completa: VPC compartilhada, EKS, RDS, Lambda, Traefik, OTel → New Relic.

### 🗣️ Fala-base
> "A Fase 3 começa com a **separação em 4 repositórios independentes**, cada um com seu próprio CI/CD. Aqui estão eles na minha organização GitHub:
>
> - **`fiap-tc-mecanica-app`** — a aplicação Spring Boot principal
> - **`fiap-tc-mecanica-infra-k8s`** — Terraform do cluster EKS, Traefik, OpenTelemetry, dashboards
> - **`fiap-tc-mecanica-infra-db`** — Terraform do RDS PostgreSQL gerenciado
> - **`fiap-tc-mecanica-lambda`** — Function Serverless de autenticação CPF→JWT
>
> Eles compartilham estado via `terraform_remote_state` — o `infra-k8s` é a **fonte única da VPC**, e os outros stacks leem dali, evitando duplicação. **A topologia final na AWS** é essa aqui no diagrama:"

### 🖥️ Tela compartilhada
1. (45s) Browser na página `https://github.com/clefern?tab=repositories` filtrada por `fiap-tc-mecanica`
2. (75s) PDF página **6-7 (Arquitetura — 4 repo-cards + state sharing)** OU página **10 (Topologia Cloud)**

### 👉 Cliques / destaques
- No GitHub: passar o mouse por cada um dos 4 repos rapidamente (5s cada)
- No PDF/diagramas: cursor circular sobre o "**single source of truth**" e setas `terraform_remote_state`
- Highlight no diagrama de topologia: trace o fluxo `Traefik → API Gateway → Lambda → RDS` com o cursor

### ⚠️ Atenção
- Não abrir cada README individualmente (perda de tempo) — fica pro avaliador clicar depois
- Não detalhar Terraform aqui (Bloco 3 cobre CI/CD que dispara o Terraform)

---

## ⏱️ Bloco 3 — CI/CD: Provision & Deploy Orquestrado (2:45 → 4:45 · 2min)

### 🎯 Objetivo
Avaliador entende que o **CD é centralizado** no `infra-k8s/cd.yml` e provisiona toda a infra + deploy em sequência (setup → db → lambda → k8s → app) em 1 clique.

### 🗣️ Fala-base
> "O **CI/CD** é orquestrado de forma centralizada. No repositório `infra-k8s`, temos um workflow chamado **`Provision & Deploy`** que dispara toda a cadeia em sequência:
>
> 1. Setup do cluster EKS (Terraform)
> 2. Deploy do RDS (consumindo o state do passo 1)
> 3. Deploy da Lambda (consumindo o state dos passos 1 e 2)
> 4. Apply dos manifestos K8s (Traefik IngressRoute, OTel Collector, app deployment)
> 5. Build e push da imagem do app no ECR + rollout no EKS
>
> Tudo isso com **1 clique no Actions UI**, ou via `gh workflow run`. Olha aqui um run completo bem-sucedido:"

### 🖥️ Tela compartilhada
1. (30s) Aba do GitHub Actions: `https://github.com/clefern/fiap-tc-mecanica-infra-k8s/actions/workflows/cd.yml`
2. (60s) Abrir o último run **success** e mostrar os 5 jobs em verde com os tempos de cada um
3. (30s) Voltar e abrir o `cd.yml` em **Code view**: `https://github.com/clefern/fiap-tc-mecanica-infra-k8s/blob/main/.github/workflows/cd.yml`
   - Scroll lento mostrando os `jobs:` na ordem (setup → db → lambda → k8s → app)

### 👉 Cliques / destaques
- Botão **"Run workflow"** no canto direito do workflow (mostrar mas **não clicar**)
- Cursor sobre cada job verde no run mais recente
- No YAML, highlight nos `needs:` entre jobs (mostra a ordem garantida)

### ⌨️ Comando alternativo (se quiser mostrar terminal)
```bash
gh workflow run cd.yml \
  --repo clefern/fiap-tc-mecanica-infra-k8s \
  -f environment=lab
```

### ⚠️ Atenção
- **Não disparar** um run novo na hora — vai demorar 10-15min e estourar o tempo do vídeo
- Mostrar um run histórico já concluído com tudo verde
- Mencionar branch protection ativa rapidamente (vai detalhar no Bloco 7)

---

## ⏱️ Bloco 4 — Autenticação CPF: Showcase Técnico (4:45 → 7:30 · 2min45s)

### 🎯 Objetivo
Avaliador vê o **fluxo de auth CPF rodando ao vivo** (ou via Postman) e entende a **decisão arquitetural** (Opção A do ADR-032).

### 🗣️ Fala-base
> "Agora a estrela da Fase 3: **autenticação de cliente via CPF** com Function Serverless.
>
> O fluxo é assim: o cliente faz `POST /auth` no **Traefik**, que roteia pra **API Gateway AWS**, que invoca a **Lambda**. A Lambda valida o CPF com módulo 11, busca o cliente no RDS, e emite um **JWT HS256** com a mesma secret do app. Daí o cliente usa esse JWT em qualquer endpoint `/api/*` do app — e o **`JwtAuthenticationFilter` aceita transparentemente**, porque a chave é a mesma.
>
> Essa é a **Opção A** do **ADR-032** — registramos por que escolhemos ela em vez de criar um proxy no app, ou implementar inline, ou usar Cognito. O ponto-chave: **o app não ganhou nenhum controller novo**. Vamos ver rodando:"

### 🖥️ Tela compartilhada
1. (30s) PDF página **8 — Diagrama de Sequência da Auth CPF** (ler com o cursor seguindo as setas)
2. (60s) Postman/Insomnia — executar `POST /auth` com `{"cpf": "529.982.247-25"}`
   - Mostrar a **response 200** com `access_token` no JSON
3. (45s) Postman — copiar o `access_token`, colar no header da próxima requisição
   - Executar `GET /api/clientes/documento/529.982.247-25`
   - Mostrar a **response 200** com os dados do cliente
4. (30s) VS Code: abrir `app/src/main/java/com/fiap/mecanica/infra/config/security/JwtAuthenticationFilter.java`
   - Destacar que **NÃO** há mudança específica pra Lambda — `loadUserByUsername(email)` é o que faz a mágica

### 👉 Cliques / destaques
- No diagrama de sequência: setas amarelo/destaque seguindo `Cliente → Traefik → Lambda → RDS → JWT`
- No JSON da response do `/auth`: highlight no `subject` do JWT (decodificar mostrando que é o email)
- No `JwtAuthenticationFilter.java`: destaque na linha `userDetailsService.loadUserByUsername(userEmail)`

### ⌨️ Comandos curl (alternativa ao Postman)
```bash
# Fluxo completo em 2 comandos:
TOKEN=$(curl -s -X POST http://localhost:8080/auth \
  -H 'Content-Type: application/json' \
  -d '{"cpf":"529.982.247-25"}' | jq -r .access_token)

curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/clientes/documento/529.982.247-25 | jq
```

### ⚠️ Atenção
- **Testar antes de gravar** — se a Lambda estiver fora do ar em lab, usar o teste de integração `JwtFromExternalIssuerIT` rodando local como prova alternativa
- Não decodificar o JWT em sites online (vazar secret) — usar `jq` local ou só apontar no payload

### 🔁 Plano B se a Lambda não responder
```bash
cd /Users/cleberfernandes/Workspace/fiap-tc-mecanica/app
./mvnw test -Dtest=JwtFromExternalIssuerIT
```
Mostrar os 3 testes verdes em ~22s.

---

## ⏱️ Bloco 5 — APIs Protegidas + Swagger (7:30 → 9:00 · 1min30s)

### 🎯 Objetivo
Avaliador vê o **catálogo de APIs** e o detalhe do **AuthController.getToken marcado @Deprecated** com referência ao ADR-032.

### 🗣️ Fala-base
> "Com o JWT em mãos, o cliente consome qualquer endpoint protegido. O **Swagger** documenta tudo — mais de 50 endpoints. Aqui está o catálogo:
>
> Note que o endpoint `POST /oauth/token` (email+senha) agora aparece **marcado como Deprecated** — clientes devem usar o `/auth` da Lambda. Ele continua funcional pra staff interna (admin, atendente, mecânico) que ainda loga por senha."

### 🖥️ Tela compartilhada
1. (60s) Swagger UI local: `http://localhost:8080/swagger-ui.html`
   - Scroll por categorias (Autenticação, Clientes, Veículos, OS, Orçamento, Estoque)
   - Clicar em **POST /oauth/token** — mostrar o badge **"deprecated"** + descrição
2. (30s) Clicar em algum endpoint protegido (ex: `GET /api/clientes/documento/{documento}`)
   - Mostrar o cadeado de auth + descrição
   - Mostrar nas **security schemes** que aceita Bearer JWT

### 👉 Cliques / destaques
- Cursor sobre o badge **DEPRECATED** em `/oauth/token`
- Expandir a descrição do endpoint pra mostrar a nota explicativa apontando pro `/auth` Lambda

### ⚠️ Atenção
- Não tentar autenticar pelo Swagger UI ao vivo (pode bugar) — só mostrar o catálogo

---

## ⏱️ Bloco 6 — Observabilidade: New Relic em Tempo Real (9:00 → 12:00 · 3min)

### 🎯 Objetivo
Avaliador vê **dashboards New Relic ao vivo**, **logs estruturados com correlationId** e os **3 alertas configurados**.

### 🗣️ Fala-base
> "Observabilidade é o pilar mais robusto da Fase 3. Usamos **OpenTelemetry Collector** como camada de coleta in-cluster, exportando pra **New Relic** via OTLP — sem lock-in, podemos trocar de vendor sem mudar código do app.
>
> Temos **2 dashboards como código** (Terraform): um de **negócio** e um de **infraestrutura**. E **3 alertas críticos** em NRQL: taxa de erro, falha de processamento de OS, e perda de sinal."

### 🖥️ Tela compartilhada
1. (60s) New Relic UI — Dashboard **"Mecânica — Visão Geral de Negócio"**
   - Mostrar widget "OS criadas (24h)", "Latência APIs (p95)", "Ciclo de vida por status"
2. (45s) New Relic UI — Dashboard **"Mecânica — Infraestrutura (OTel/K8s)"**
   - Mostrar CPU/memória por pod, JVM heap, container restarts
3. (30s) New Relic UI — **Alerts & AI** → Policy "mecanica_policy"
   - Mostrar as 3 condições: `high_error_rate`, `os_process_failure`, `app_downtime`
4. (45s) New Relic UI — **Logs** filtrados por `app.name=mecanica`
   - Mostrar log JSON estruturado com `correlationId` destacado
   - Filtrar por 1 correlationId específico pra mostrar a request inteira amarrada

### 👉 Cliques / destaques
- Highlight no `correlationId` do log — explicar que **propaga entre logs, traces e métricas** via MDC
- Mostrar o **arquivo IaC** que define os dashboards: rapidamente abrir `infra-k8s/infra/conf/17-observability.tf` em uma aba — só pra mostrar que **é código, não cliques na UI**

### ⌨️ Comando alternativo (mostrar OTel Collector rodando)
```bash
kubectl get pods -n mecanica-lab -l app=otel-collector
kubectl logs -n mecanica-lab daemonset/otel-collector --tail=20
```

### ⚠️ Atenção
- Se a conta New Relic tiver pouco dado (lab idle), **gerar tráfego antes** de gravar:
  ```bash
  for i in {1..100}; do curl -s http://localhost:8080/actuator/health > /dev/null; done
  ```
- Não navegar muito tempo na UI do NR — escolher 3-4 widgets que conta a história

### 🔁 Plano B se a UI do New Relic estiver lenta
Mostrar **screenshot** dos dashboards salvo previamente em `docs/entrega/fase-3/screenshots/` (criar antes de gravar).

---

## ⏱️ Bloco 7 — Governança: Branch Protection + Documentação (12:00 → 13:30 · 1min30s)

### 🎯 Objetivo
Avaliador entende que **`main` é protegida**, **PR obrigatório**, e que toda decisão técnica está **registrada em ADRs e RFCs**.

### 🗣️ Fala-base
> "Pra fechar, **governança**. Os 4 repositórios têm **branch protection** ativa em `main` e `develop` — PR obrigatório, 1 approval mínimo, sem force push ou direct push. O usuário **`soat-architecture`** está adicionado como collaborator nos 4, conforme exigido pela rubrica.
>
> Todas as decisões da Fase 3 estão formalizadas: **4 novos ADRs** — Auth CPF, Traefik, Observabilidade, RDS — e **3 RFCs** que avaliaram alternativas antes de cada decisão."

### 🖥️ Tela compartilhada
1. (30s) GitHub: `https://github.com/clefern/fiap-tc-mecanica-app/settings/branches`
   - Mostrar regra ativa em `main`: "Require pull request before merging" + "Require status checks" + "Do not allow force pushes"
   - Mostrar tab **Collaborators** → `soat-architecture` listado
2. (45s) VS Code: árvore `app/docs/ADRs/`
   - Destacar **ADR-032** (auth-cpf-via-lambda), **ADR-033** (traefik), **ADR-034** (otel-newrelic), **ADR-035** (rds)
   - Abrir o `ADR-032` brevemente, mostrar a seção **"Alternativas consideradas"**
3. (15s) VS Code: árvore `app/docs/RFCs/`
   - Mostrar RFC-001, 002, 003

### 👉 Cliques / destaques
- No GitHub Settings: cursor circulando "Require pull request" + "1 approving review required"
- No ADR-032: scroll mostrando a tabela "Opção A vs B vs C vs D" com justificativa

### ⚠️ Atenção
- Não abrir todos os ADRs (são 35) — só mencionar o total e mostrar os 4 novos

---

## ⏱️ Bloco 8 — Encerramento (13:30 → 15:00 · 1min30s)

### 🎯 Objetivo
Avaliador sai do vídeo sabendo **onde encontrar tudo** (PDF, repos, vídeo, docs) e tem o **resumo do que evoluiu** em relação às fases anteriores.

### 🗣️ Fala-base
> "Recapitulando: a Mecânica API evoluiu de um **monolito local** na Fase 1 para um sistema **cloud-native single-repo** na Fase 2, e agora na **Fase 3** é um sistema **distribuído de operação corporativa** — 4 repos, autenticação serverless, banco gerenciado, API Gateway profissional, observabilidade ponta a ponta e governança formal.
>
> Todos os artefatos estão no **PDF de entrega**, com links pros 4 repos, este vídeo, e a documentação completa. Obrigado pela atenção, **Grupo 14SOAT**."

### 🖥️ Tela compartilhada
1. (30s) PDF página **2 (Links de Entrega)** — passar o cursor por cada link
2. (30s) PDF página **5 (estatísticas: 35 ADRs · 3 RFCs · 28/28 itens)**
3. (30s) PDF página **15 (Equipe + encerramento)** — fica fixo até o vídeo cortar

### 👉 Cliques / destaques
- Cursor sobre cada um dos 4 repo-cards na pg 2 (1 segundo cada)
- Cursor sobre o badge "**28/28**" nas estatísticas (transmite completude)
- Última imagem fixa: capa de encerramento

### ⚠️ Atenção
- Não falar "espero que tenham gostado" — direto e profissional
- Garantir **silêncio** nos últimos 3-5 segundos pra editor cortar limpo

---

## 📋 Resumo de tempo (target vs orçamento)

| Bloco | Duração | Cumulativo |
|---|---|---|
| 1. Abertura | 0:45 | 0:45 |
| 2. Arquitetura 4 repos + topologia | 2:00 | 2:45 |
| 3. CI/CD Provision & Deploy | 2:00 | 4:45 |
| 4. Auth CPF showcase técnico | 2:45 | 7:30 |
| 5. APIs protegidas + Swagger | 1:30 | 9:00 |
| 6. Observabilidade New Relic | 3:00 | 12:00 |
| 7. Governança + ADRs/RFCs | 1:30 | 13:30 |
| 8. Encerramento | 1:30 | 15:00 |

**Total**: 15:00 exatos. Cada bloco tem 5-10s de folga interna pra respiro.

## 🚨 Se o vídeo passar de 15min na primeira tomada

**Cortes em ordem de prioridade** (do menos doloroso pro mais):
1. Reduzir Bloco 5 (Swagger) de 1:30 → 1:00 (só mostra catálogo, sem expandir endpoint)
2. Reduzir Bloco 6 (New Relic) de 3:00 → 2:30 (cortar logs, deixar só dashboards + alertas)
3. Reduzir Bloco 7 (Governança) de 1:30 → 1:00 (não abrir ADR-032, só listar)
4. **Último recurso**: reduzir Bloco 4 (Auth CPF) — **não recomendado**, é a estrela técnica

## 🎬 Pós-gravação

- [ ] Editar cortando "ééé", "tipo assim", silêncios > 2s
- [ ] Adicionar **legendas em PT-BR** (vai pontuar acessibilidade)
- [ ] Adicionar **chapters** no YouTube (1 por bloco — usa os timestamps acima)
- [ ] Upload como **não-listado** em YouTube
- [ ] Substituir URL placeholder no PDF e re-gerar:
  ```bash
  cd /Users/cleberfernandes/Workspace/fiap-tc-mecanica/app/docs/entrega/fase-3
  sed -i '' 's|youtu.be/PENDING-FASE-3|youtu.be/SUA-URL-AQUI|g' presentation-fase3.html
  ./gerar-pdf-fase3.sh
  ```
- [ ] Submeter PDF atualizado + link do vídeo no **Portal do Aluno**

## 📎 Apêndice — Links úteis ao apresentador

| O quê | Onde |
|---|---|
| PDF de entrega (este diretório) | `app/docs/entrega/fase-3/entrega-fase3-grupo14soat.pdf` |
| Diagramas de arquitetura | `app/docs/arquitetura/diagramas.md` |
| ADR-032 Auth CPF | `app/docs/ADRs/ADR-032-autenticacao-cpf-via-lambda.md` |
| ADR-033 Traefik | `app/docs/ADRs/ADR-033-traefik-api-gateway.md` |
| ADR-034 OTel + New Relic | `app/docs/ADRs/ADR-034-observabilidade-opentelemetry-newrelic.md` |
| ADR-035 RDS gerenciado | `app/docs/ADRs/ADR-035-rds-postgresql-gerenciado.md` |
| RFCs Fase 3 | `app/docs/RFCs/` |
| Teste de integração JWT externo | `app/src/test/java/com/fiap/mecanica/integration/JwtFromExternalIssuerIT.java` |
| `cd.yml` Provision & Deploy | `infra-k8s/.github/workflows/cd.yml` |
| Lambda handler | `lambda/src/handler.ts` |
| Lambda Terraform | `lambda/infra/conf/2-lambda-auth.tf` |
| Traefik IngressRoute | `infra-k8s/k8s/base/app-ingressroute.yaml` |
| OTel Collector ConfigMap | `infra-k8s/k8s/base/otel-collector-configmap.yaml` |
| Dashboards New Relic IaC | `infra-k8s/infra/conf/17-observability.tf` |
