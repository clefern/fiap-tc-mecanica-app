# Roteiro CURTO do vídeo demonstrativo — Fase 3 (Tech Challenge 13SOAT)

> **Duração-alvo**: 9 minutos (limite duro 10:00) · **Versão completa** (15 min): [`roteiro-video-fase3.md`](./roteiro-video-fase3.md)

## Quando usar este roteiro

Quando o requisito de tempo apertar (≤ 10 min), gravação pra apresentação interna, ou versão "preview" antes do vídeo oficial. **Foco**: mostrar mais do que falar — **3 demos ao vivo + 3 telas-chave do PDF**.

## O que ficou de fora (vs versão de 15 min)

| Cortado | Por quê | Onde está |
|---|---|---|
| Bloco "APIs protegidas + Swagger" inteiro | Mesclado no demo de Auth CPF (mostra catálogo brevemente após o `curl`) | Bloco 4 deste roteiro |
| Detalhamento de ADRs alternativos (A vs B vs C vs D) | Avaliador lê no PDF + repo | Bloco 6 menciona em 1 frase |
| Diagrama de sequência ASCII do PDF | Slide pesado, ocupa 30s sem demo | Bloco 4 substitui por execução real |
| Logs JSON do New Relic com correlationId | Detalhe técnico — fica pra versão longa | — |
| Branch protection passo-a-passo no Settings | Avaliador valida via API/UI depois | Bloco 6 menciona em 5s |

## Preparação antes de gravar (essencial)

- [ ] **3 abas** no browser (na ordem): GitHub (`clefern?tab=repositories`), Actions UI (`infra-k8s/.../cd.yml`), New Relic (dashboard "Mecânica — Visão Geral")
- [ ] Postman/Insomnia com 2 requisições prontas: `POST /auth` e `GET /api/clientes/documento/{cpf}`
- [ ] App rodando local (Docker Compose) com cliente seedado `CPF 529.982.247-25`
- [ ] PDF de entrega em uma 4ª aba (vai usar só pgs **1**, **10** e **15**)
- [ ] **Gerar tráfego** no New Relic 5min antes: `for i in {1..200}; do curl -s http://localhost:8080/actuator/health > /dev/null; done`
- [ ] Notificações silenciadas, câmera desligada, fonte do terminal ≥ 16pt

---

## ⏱️ Bloco 1 — Abertura ultra-rápida (0:00 → 0:30 · 30s)

### 🗣️ Fala-base (1 parágrafo só)
> "**Grupo 14SOAT** apresentando a **Fase 3 da Mecânica API**. O foco da fase é elevar o sistema a **operação corporativa**: 4 repositórios independentes, autenticação serverless por CPF, banco gerenciado, observabilidade ponta a ponta. Em 9 minutos eu mostro tudo rodando."

### 🖥️ Tela
PDF página **1 (capa)** — fixa por 15s

### 👉 Destaques
Cursor passa rápido nos chips da stack na capa (3 segundos).

### ⚠️ Atenção
**Cortar em 30s** — qualquer segundo a mais aqui sai do tempo de demo.

---

## ⏱️ Bloco 2 — Arquitetura: 4 repos + topologia em 1 slide (0:30 → 1:45 · 1min15s)

### 🗣️ Fala-base
> "São **4 repositórios independentes**: `app` (Spring Boot), `infra-k8s` (EKS + Traefik + OTel), `infra-db` (RDS) e `lambda` (Function de auth). Cada um com CI/CD próprio. Todos compartilham estado via `terraform_remote_state` — o `infra-k8s` é a **fonte única da VPC**.
>
> A topologia cloud final é essa: Traefik na borda, EKS rodando o app com OpenTelemetry, Lambda atrás de API Gateway pra autenticação CPF, RDS gerenciado, tudo exportando pra New Relic."

### 🖥️ Tela
1. (20s) `https://github.com/clefern?tab=repositories` filtrado por `fiap-tc-mecanica` — mostrar os 4 cards
2. (55s) PDF página **10 (Topologia Cloud AWS — diagrama ASCII completo)** — cursor traça o fluxo `Traefik → API GW → Lambda → RDS` e `App → OTel Collector → New Relic`

### 👉 Destaques
- 4 repos no GitHub: cursor por cada um (3-4s cada)
- Diagrama: NÃO ler caixa por caixa — apontar os 2 fluxos principais

### ⚠️ Atenção
Pular diagramas C4 / 4 repo-cards detalhados da pg 6-7 — vai pra versão longa.

---

## ⏱️ Bloco 3 — CI/CD: 1 run verde mostrando a cadeia (1:45 → 2:45 · 1min)

### 🗣️ Fala-base
> "O CD é orquestrado num único workflow chamado **Provision & Deploy**, no `infra-k8s`. Um clique no GitHub Actions provisiona tudo em sequência: cluster EKS, RDS, Lambda, manifestos K8s e finalmente build+rollout do app no EKS. Aqui está um run completo, todo verde:"

### 🖥️ Tela
1. (35s) `https://github.com/clefern/fiap-tc-mecanica-infra-k8s/actions/workflows/cd.yml` — abrir último run **success**
2. (25s) Scroll mostrando os **5 jobs verdes** em sequência (setup → db → lambda → k8s → app) com os tempos

### 👉 Destaques
- Botão **"Run workflow"** (mostrar, **não clicar**)
- Cursor passa rápido pelos 5 jobs verdes

### ⚠️ Atenção
**NÃO disparar** run novo — pega 10-15min, estoura o vídeo. Mostrar um histórico.

---

## ⏱️ Bloco 4 — Auth CPF DEMO ao vivo + APIs em 1 fluxo (2:45 → 5:15 · 2min30s)

### 🗣️ Fala-base
> "A estrela técnica da Fase 3: **autenticação de cliente por CPF via Function Serverless**. O fluxo é simples: o cliente faz `POST /auth` no Traefik, que roteia pra Lambda, que valida o CPF, busca o cliente no RDS e emite um JWT HS256. Esse JWT é aceito **transparentemente** pelos endpoints `/api/*` do app, porque a secret é a mesma.
>
> A decisão arquitetural — registrada no ADR-032 — foi não criar nenhum controller novo no app: o `JwtAuthenticationFilter` que já existia desde a Fase 1 valida o token da Lambda sem nenhuma mudança. Vamos ver rodando:"

### 🖥️ Tela
1. (60s) **Postman/Insomnia** — executar `POST /auth` com `{"cpf": "529.982.247-25"}` → mostrar response 200 com `access_token`
2. (45s) **Copiar o token**, executar `GET /api/clientes/documento/529.982.247-25` → mostrar response 200 com dados do cliente
3. (45s) **Swagger UI** (`http://localhost:8080/swagger-ui.html`) — scroll rápido pelo catálogo, parar em `POST /oauth/token` mostrando o badge **DEPRECATED** (clientes usam `/auth` agora; staff segue por senha)

### ⌨️ Comandos curl (alternativa ao Postman)
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth \
  -H 'Content-Type: application/json' \
  -d '{"cpf":"529.982.247-25"}' | jq -r .access_token)

curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/clientes/documento/529.982.247-25 | jq
```

### ⚠️ Atenção
- **Testar 1h antes da gravação** — se Lambda falhar, plano B: `./mvnw test -Dtest=JwtFromExternalIssuerIT` em ~22s (3 testes verdes provando o mesmo)
- Não decodificar JWT em site online (vaza secret) — só apontar no payload

### 🔁 Plano B (se Lambda fora do ar)
Pula direto pro teste de integração:
```bash
cd /Users/cleberfernandes/Workspace/fiap-tc-mecanica/app
./mvnw test -Dtest=JwtFromExternalIssuerIT
```
Mostra 3 cenários: JWT válido aceito, subject desconhecido rejeitado, secret diferente rejeitado.

---

## ⏱️ Bloco 5 — Observabilidade: 2 dashboards + 3 alertas (5:15 → 7:15 · 2min)

### 🗣️ Fala-base
> "Observabilidade ponta a ponta: **OpenTelemetry Collector** roda como DaemonSet no cluster, coletando traces, métricas e logs, e exportando pra **New Relic**. **Dois dashboards**, definidos como código no Terraform — um de negócio e um de infraestrutura. E **três alertas críticos** em NRQL."

### 🖥️ Tela
1. (45s) New Relic UI — Dashboard **"Mecânica — Visão Geral de Negócio"**
   - Apontar widgets: OS criadas (24h), latência APIs p95, ciclo de vida por status
2. (40s) New Relic UI — Dashboard **"Infraestrutura (OTel/K8s)"**
   - Apontar: CPU/mem por pod, JVM heap, container metrics
3. (25s) New Relic UI — **Alerts** → Policy "mecanica_policy"
   - Listar os 3 alertas: `high_error_rate`, `os_process_failure`, `app_downtime`
4. (10s) Pisca rapidíssimo em `infra-k8s/infra/conf/17-observability.tf` no GitHub — "isso tudo é código"

### ⚠️ Atenção
- **Gerar tráfego antes** de gravar pra dashboards terem dados (comando no checklist de preparação)
- Plano B: screenshots em `docs/entrega/fase-3/screenshots/` se NR estiver lento

---

## ⏱️ Bloco 6 — Governança + ADRs + Encerramento (7:15 → 8:45 · 1min30s)

### 🗣️ Fala-base
> "Pra fechar: **governança formal**. Os 4 repos têm branch protection ativa em `main` e `develop`, PR obrigatório com 1 approval, e o usuário `soat-architecture` está adicionado como collaborator nos 4 — exigido pela rubrica.
>
> Todas as decisões da Fase 3 estão formalizadas: **4 novos ADRs** (auth CPF, Traefik, OTel+New Relic, RDS) e **3 RFCs** que avaliaram alternativas antes de decidir. Tudo no repositório do app, em `docs/ADRs/` e `docs/RFCs/`.
>
> Os links de todos os entregáveis, incluindo este vídeo, estão no PDF de entrega. **Grupo 14SOAT**, obrigado pela atenção."

### 🖥️ Tela
1. (30s) VS Code: árvore `app/docs/` mostrando pastas `ADRs/` (destacar 032..035 novos) e `RFCs/` (3 arquivos)
2. (30s) PDF página **2 (Links de Entrega)** — cursor passa por cada um dos 4 repos
3. (30s) PDF página **15 (encerramento)** — fixa até cortar

### ⚠️ Atenção
- **NÃO** abrir um ADR individual (consome tempo) — só listar
- Último frame: capa de encerramento fixa por 3-5s pra editor cortar limpo

---

## 📋 Resumo de tempo (target vs orçamento)

| Bloco | Duração | Cumulativo |
|---|---|---|
| 1 Abertura | 0:30 | 0:30 |
| 2 Arquitetura + topologia (1 slide) | 1:15 | 1:45 |
| 3 CI/CD (1 run verde) | 1:00 | 2:45 |
| 4 Auth CPF DEMO + Swagger | 2:30 | 5:15 |
| 5 Observabilidade (2 dashboards + alertas) | 2:00 | 7:15 |
| 6 Governança + Encerramento | 1:30 | 8:45 |

**Total**: 8:45 (com 15s de margem para o limite de 9:00). Limite duro 10:00 → 1:15 de folga total se algum bloco estourar.

## 🚨 Se passar de 9:00 na primeira tomada

**Cortes em ordem de prioridade**:
1. Reduzir Bloco 5 de 2:00 → 1:30 (mostrar só 1 dashboard + alertas, pula o de infra)
2. Reduzir Bloco 2 de 1:15 → 0:45 (só 1 frase sobre os repos, vai direto pra topologia)
3. Reduzir Bloco 6 de 1:30 → 1:00 (pular tour pelos ADRs, ir direto pro PDF de encerramento)
4. **Último recurso**: cortar Swagger do Bloco 4 (mas é onde aparece o `@Deprecated` — perde-se um destaque)

## 🎬 Pós-gravação

- [ ] Cortar silêncios > 1.5s (mais agressivo que na versão longa)
- [ ] Adicionar legendas PT-BR
- [ ] **6 chapters no YouTube** (1 por bloco usando timestamps acima)
- [ ] Upload como **não-listado**
- [ ] Substituir URL placeholder no PDF e re-gerar (vide `roteiro-video-fase3.md` apêndice)

## 📎 Diferenças-chave vs versão longa (15 min)

| Aspecto | Versão longa | Versão curta |
|---|---|---|
| Blocos | 8 | 6 |
| Demos ao vivo | 1 (auth CPF) | 1 (auth CPF) — mesma |
| Slides PDF mostrados | 6 | 3 (cover, topologia, encerramento) |
| Detalhamento ADRs | Tour pelo ADR-032 | Só lista |
| Logs JSON do New Relic | Sim | Não |
| Branch protection passo-a-passo | Sim (no Settings) | Mencionado em 5s |
| Diagrama de sequência ASCII | Sim (do PDF) | Não — substituído pela demo real |

A versão curta **mantém os 4 pilares-chave** (4 repos, CI/CD, Auth CPF, Observabilidade) e **enxuga apenas o didatismo extra** que ficava bonito mas não muda a pontuação da rubrica.
