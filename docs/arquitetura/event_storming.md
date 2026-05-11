# Event Storming — Sistema Oficina Mecânica

> Mapeamento dos eventos de domínio, comandos e políticas que regem o sistema.
> Reflete a implementação atual (Spring `ApplicationEvent`).

---

## Legenda

| Símbolo | Significado |
|---------|-------------|
| 🟠 Evento | Algo que aconteceu no passado (fato imutável) |
| 🔵 Comando | Intenção de fazer algo acontecer |
| 🟡 Agregado | Processa o comando e gera o evento |
| 🟣 Política | Reage a um evento e dispara outro comando |
| 🔴 Hotspot | Ponto de atenção ou decisão não trivial |
| 🟢 Sistema Externo | Ator ou sistema fora do domínio principal |

---

## Fluxo Principal: Criação → Entrega

```
ATOR              COMANDO                  AGREGADO         EVENTO
────────────────────────────────────────────────────────────────────────────────

[Atendente]  ──► AbrirOrdemServico    ──► OrdemServico ──► 🟠 OsCriadaEvent
             ──► AdicionarItens       ──► OrdemServico ──► (sem evento Spring)

[Mecânico]   ──► IniciarDiagnostico   ──► OrdemServico ──► (sem evento Spring)
                 (OS: RECEBIDA → EM_DIAGNOSTICO)

[Mecânico]   ──► EmitirOrcamento      ──► Orcamento    ──► 🟠 OrcamentoGeradoEvent
                 (OS: EM_DIAGNOSTICO → AGUARDANDO_APROVACAO)
                                                        ──► 🟠 OrdemServicoAguardandoAprovacaoEvent

                 🟣 NotificarCliente (ao receber OrcamentoGeradoEvent)
                    → Envia email com links tokenizados (HMAC-SHA256):
                      "Aprovar Orçamento" e "Recusar Orçamento"

[Cliente]    ──► AprovarOrcamento     ──► Orcamento    ──► 🟠 OrcamentoAprovadoEvent
  via link        (via link de email                    ──► 🟠 OrdemServicoAprovadaEvent
  de email        ou via API M2M)
  ou API M2M      (OS: AGUARDANDO_APROVACAO → APROVADA)

                 🟣 BaixarEstoque (ao receber OrcamentoAprovadoEvent)
                    → ItemEstocavel.baixarEstoque(quantidade) para cada item

[Mecânico]   ──► IniciarExecucao      ──► OrdemServico ──► (sem evento Spring)
                 (OS: APROVADA → EM_EXECUCAO)

[Mecânico]   ──► FinalizarOS          ──► OrdemServico ──► 🟠 OsFinalizadaEvent
                 (OS: EM_EXECUCAO → FINALIZADA)

                 🟣 NotificarFinalizacao (ao receber OsFinalizadaEvent)
                    → Envia email de notificação de conclusão ao cliente

[Atendente]  ──► EntregarVeiculo      ──► OrdemServico ──► (sem evento Spring)
                 (OS: FINALIZADA → ENTREGUE)

────────────────────────────────────────────────────────────────────────────────
CANCELAMENTO (qualquer estado exceto ENTREGUE):

[Qualquer]   ──► CancelarOS           ──► OrdemServico ──► 🟠 OrdemServicoCanceladaEvent

────────────────────────────────────────────────────────────────────────────────
REJEIÇÃO DE ORÇAMENTO:

[Cliente]    ──► RejeitarOrcamento    ──► Orcamento    ──► 🟠 OrcamentoReprovadoEvent
                 (OS: AGUARDANDO_APROVACAO → CANCELADA ou retorno a EM_DIAGNOSTICO)
```

---

## Eventos de Domínio (Spring ApplicationEvent)

Estes são os únicos eventos publicados via `ApplicationEventPublisher`. Listeners usam `@TransactionalEventListener` + `@Async`.

| Evento | Payload | Trigger | Listener / Efeito |
|--------|---------|---------|-------------------|
| `OsCriadaEvent` | `OrdemServico` | OS criada | Email de confirmação de recebimento |
| `OrdemServicoAguardandoAprovacaoEvent` | `OrdemServico` | OS entra em AGUARDANDO_APROVACAO | — (complementa OrcamentoGeradoEvent) |
| `OrdemServicoAprovadaEvent` | `OrdemServico` | Orçamento aprovado | Baixa de estoque |
| `OsFinalizadaEvent` | `OrdemServico` | OS entra em FINALIZADA | Email de notificação ao cliente |
| `OrdemServicoCanceladaEvent` | `OrdemServico` | OS cancelada | Estorno de estoque (se aplicável) |
| `OrcamentoGeradoEvent` | `Orcamento` | Orçamento emitido | Email com links de aprovação/recusa |
| `OrcamentoAprovadoEvent` | `Orcamento` | Orçamento aprovado pelo cliente | Dispara aprovação da OS |
| `OrcamentoReprovadoEvent` | `Orcamento` | Orçamento rejeitado pelo cliente | OS pode ser cancelada ou revisada |

---

## Fluxo de Aprovação via Email (HMAC Token)

```
[Mecânico emite orçamento]
      │
      ▼
🟠 OrcamentoGeradoEvent
      │
      ▼ (listener assíncrono)
[NotificacaoEmailApplicationService]
  → ActionTokenService.generate(orcamentoId, "APROVADO", 1440min) → token A
  → ActionTokenService.generate(orcamentoId, "REJEITADO", 1440min) → token R
  → Envia email com:
    - Link A: GET /api/integracoes/orcamentos/aprovacao?token=<A>&decisao=APROVADO
    - Link R: GET /api/integracoes/orcamentos/aprovacao?token=<R>&decisao=REJEITADO
      │
      ▼
[Cliente clica no link]
[IntegracaoOrcamentoController]
  → ActionTokenService.validate(orcamentoId, decisao, token)
  → OrcamentoService.aprovar/reprovar(ordemServicoId)
  → publica OrcamentoAprovadoEvent ou OrcamentoReprovadoEvent
```

---

## Integração M2M (API Key)

```
🟢 [Sistema Externo]
      │ POST /api/integracoes/orcamentos/aprovacao
      │ Header: X-Api-Key: <chave configurada>
      ▼
[IntegracaoOrcamentoController]
  → ApiKeyAuthFilter valida o header
  → OrcamentoService.aprovar/reprovar(...)
  → publica OrcamentoAprovadoEvent ou OrcamentoReprovadoEvent
```

---

## Políticas (Regras Reativas)

| Política | Trigger | Ação |
|----------|---------|------|
| **NotificarClienteOrcamento** | `OrcamentoGeradoEvent` | Gera tokens HMAC e envia email com links de ação |
| **BaixarEstoqueNaAprovacao** | `OrcamentoAprovadoEvent` | Baixa estoque de peças/insumos da OS (ADR-019) |
| **NotificarFinalizacao** | `OsFinalizadaEvent` | Email informativo ao cliente |
| **AlertarEstoqueBaixo** | Após baixa de estoque | Verifica StatusEstoque e dispara alerta de reposição |
| **GerarPdfOrcamento** | `OrcamentoGeradoEvent` | Gera PDF do orçamento e salva URL |

---

## Bounded Contexts

O sistema é um monolito modular. Os bounded contexts são lógicos (separação de pacotes), não serviços independentes.

### Context: Atendimento
- **Agregados:** `OrdemServico`, `Cliente`, `Veiculo`
- **Comandos principais:** AbrirOS, AdicionarItens, EntregarVeiculo
- **Eventos principais:** `OsCriadaEvent`, `OsFinalizadaEvent`, `OrdemServicoCanceladaEvent`
- **Pacotes:**
  - `domain/model/OrdemServico`, `domain/model/Cliente`, `domain/model/Veiculo`
  - `application/service/impl/Os*ServiceImpl`
  - `presentation/controller/OrdemServicoController`, `ClienteController`, `VeiculoController`

### Context: Diagnóstico e Orçamento
- **Agregados:** `Orcamento` (vinculado à OS)
- **Comandos principais:** EmitirOrcamento, AprovarOrcamento, RejeitarOrcamento
- **Eventos principais:** `OrcamentoGeradoEvent`, `OrcamentoAprovadoEvent`, `OrcamentoReprovadoEvent`
- **Pacotes:**
  - `domain/model/Orcamento`
  - `application/service/impl/OrcamentoServiceImpl`
  - `presentation/controller/OrcamentoController`
- **Relação com Atendimento:** Customer/Supplier — OS (upstream) aciona Orçamento (downstream)

### Context: Estoque
- **Entidades:** `Peca`, `Insumo` (herdam de `ItemEstocavel`), `Servico`
- **Comandos principais:** BaixarEstoque, AdicionarEstoque
- **Eventos:** reage a `OrcamentoAprovadoEvent` para baixa; sem eventos de estoque próprios
- **Pacotes:**
  - `domain/model/Peca`, `domain/model/Insumo`, `domain/model/Servico`
  - `application/service/impl/PecaServiceImpl`, `InsumoServiceImpl`, `ServicoServiceImpl`, `EstoqueServiceImpl`
  - `presentation/controller/PecaController`, `InsumoController`, `ServicoController`, `EstoqueController`
- **Nota:** Não há entidade `MovimentacaoEstoque` — rastreamento de histórico não implementado

### Context: Integração Externa
- **Atores:** Sistemas externos (aprovação M2M), Clientes via email tokenizado
- **Mecanismo:** `ApiKeyAuthFilter` (header X-Api-Key) e `ActionTokenService` (HMAC-SHA256)
- **Pacotes:**
  - `presentation/controller/IntegracaoOrcamentoController`
  - `infra/security/ApiKeyAuthFilter`, `infra/security/ActionTokenService`

### Mapeamento Bounded Context → Packages

```
com/fiap/mecanica/
├── domain/model/
│   ├── OrdemServico, Cliente, Veiculo          → Atendimento
│   ├── Orcamento                               → Diagnóstico e Orçamento
│   └── Peca, Insumo, Servico, ItemComercial    → Estoque
├── application/service/impl/
│   ├── Os*ServiceImpl                          → Atendimento
│   ├── OrcamentoServiceImpl                    → Diagnóstico e Orçamento
│   └── Peca/Insumo/Servico/EstoqueServiceImpl  → Estoque
├── presentation/controller/
│   ├── OrdemServico/Cliente/VeiculoController  → Atendimento
│   ├── OrcamentoController                     → Diagnóstico e Orçamento
│   ├── Peca/Insumo/Servico/EstoqueController   → Estoque
│   └── IntegracaoOrcamentoController           → Integração Externa
└── infra/security/
    ├── JwtAuthenticationFilter                 → Transversal (todos os contexts)
    ├── ApiKeyAuthFilter                        → Integração Externa
    └── ActionTokenService                      → Integração Externa
```

---

## Hotspots

| Hotspot | Descrição | Como foi resolvido |
|---------|-----------|-------------------|
| **Fluxo de aprovação** | Cliente não tem acesso JWT para aprovar | Links tokenizados (HMAC-SHA256) + API Key M2M |
| **Orçamento vs OS** | Ordem de quem aprova quem | ADR-022: OS é aprovada ATRAVÉS da aprovação do orçamento |
| **Estoque** | Quando debitar o estoque | ADR-019: apenas na aprovação do orçamento, não na criação da OS |
| **Monolito vs Microsserviços** | Separação em serviços | ADR-031: mantido monolito modular; bounded contexts são lógicos |
| **Concorrência no estoque** | Duas OS aprovadas simultaneamente | Validação em `baixarEstoque()` com exceção de negócio |
| **Rejeição de orçamento** | OS deve ser cancelada ou revisada? | ADR-026: OS pode ser cancelada ou retornar para EM_DIAGNOSTICO |

---

## Sistemas Externos Integrados

| Sistema | Integração | Protocolo |
|---------|-----------|-----------|
| **MailHog / SMTP** | Envio de emails de notificação | Spring Mail + SMTP |
| **Sistemas M2M** | Aprovação externa de orçamentos | REST API Key (X-Api-Key) |

> Integrações planejadas mas não implementadas: validador externo de documentos (CPF/CNPJ), sistemas de fornecedores, gateway de pagamento.
