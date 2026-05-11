# Arquitetura — Mecânica API

## Estilo Arquitetural

**Monolito Modular** com **Arquitetura Hexagonal (Ports & Adapters)** e **DDD**.

O núcleo de domínio é completamente isolado de frameworks, banco de dados e entradas externas. Dependências apontam sempre para dentro — o domínio não conhece Spring, JPA ou REST.

```
Presentation  →  Application  →  Domain  ←  Infrastructure
(REST/DTOs)      (Use Cases)     (Core)      (JPA/Email/etc.)
```

## Estrutura de Pacotes

```
com/fiap/mecanica/
├── domain/          # Entidades, VOs, Ports (interfaces), Eventos, Exceções
├── application/     # Serviços de aplicação, DTOs, Listeners de eventos
├── infra/           # Adapters JPA, Security, Seeding, Mappers
└── presentation/    # Controllers REST, DTOs de request/response, Mappers
```

**Padrão de mapeamento em duas camadas:**
- `infra/mapper/` — domain model ↔ JPA entity
- `presentation/mapper/` — domain model ↔ request/response DTO

## Modelo de Domínio

**Aggregate root:** `OrdemServico` — ver [Linguagem Ubíqua](./arquitetura/linguagem_ubiqua.md) para a máquina de estados completa, entidades, VOs e regras de negócio.

**Entidades principais:** `OrdemServico`, `Orcamento`, `User` (→ Cliente, Atendente, Mecanico, Admin), `Veiculo`, `ItemComercial` (→ Servico, Peca, Insumo)

**Value Objects:** `CPF`, `CNPJ`, `Email`, `PlacaVeiculo`, `Endereco`, `TelefoneBr`

## Padrões Implementados

| Padrão | Aplicação |
|--------|-----------|
| Ports & Adapters | `domain/repository/` = ports; `infra/adapter/Jpa*RepositoryAdapter` = adapters |
| Domain Events | `ApplicationEvent` + `@TransactionalEventListener` + `@Async` |
| Task-based API | Ações de OS como sub-recursos: `/ordens-servico/{id}/acoes` |
| Exception hierarchy | `DomainRuleException` → 422 · `ResourceNotFoundException` → 404 |
| Object-level security | `@OsSecurity` — autorização por objeto além de role |
| API Key M2M | `IntegracaoOrcamentoController` — header `X-Api-Key` para integrações externas |
| HMAC token em email | Links de aprovação/recusa tokenizados (HMAC-SHA256 com expiração) |

## Fluxo Principal

```
1. Recepção    Cliente + veículo cadastrados → OS aberta (RECEBIDA)
2. Diagnóstico Mecânico avalia → Orçamento gerado → Email com link tokenizado ao cliente
3. Aprovação   Cliente aprova via link (HMAC token) ou via API M2M → Estoque baixado
4. Execução    Peças reservadas → Serviços executados (EM_EXECUCAO)
5. Finalização OS finalizada → Email de entrega → ENTREGUE
```

## Eventos de Domínio

`OsCriadaEvent` · `OrcamentoGeradoEvent` · `OrdemServicoAguardandoAprovacaoEvent` · `OrcamentoAprovadoEvent` · `OrcamentoReprovadoEvent` · `OsFinalizadaEvent` · `OrdemServicoCanceladaEvent`

## Segurança

- **Autenticação:** JWT (JJWT 0.12.5) via `JwtAuthenticationFilter`
- **Autorização:** `@PreAuthorize` com roles + `@OsSecurity` para object-level
- **Rate limiting:** Bucket4j (10 req/min por padrão)
- **Integração M2M:** API key via header, sem JWT

## Artefatos DDD

| Artefato | Conteúdo |
|----------|----------|
| [Linguagem Ubíqua](./arquitetura/linguagem_ubiqua.md) | Glossário completo dos termos do domínio |
| [Event Storming](./arquitetura/event_storming.md) | Eventos, comandos, políticas e fluxos temporais |
| [Diagramas C4](./arquitetura/diagramas.md) | Bounded Contexts, Context Map, diagramas de sequência |

## Decisões Arquiteturais Relevantes

| ADR | Decisão |
|-----|---------|
| [ADR-003](./ADRs/ADR-003-task-based-os-status-api.md) | API baseada em tarefas para transições de OS |
| [ADR-010](./ADRs/ADR-010-estrategia-autorizacao-hibrida.md) | Autorização híbrida: JWT roles + object-level security |
| [ADR-019](./ADRs/ADR-019-desacoplamento-orcamento-estoque.md) | Desacoplamento Orçamento-Estoque (baixa só na aprovação da OS) |
| [ADR-022](./ADRs/ADR-022-inversao-fluxo-aprovacao-os-orcamento.md) | Fluxo invertido: OS aprovada via aprovação do Orçamento |
| [ADR-031](./ADRs/ADR-031-decisoes-fase2.md) | Decisões consolidadas da Fase 2 (infra, CI/CD, M2M, monólito mantido) |

Ver [docs/decisoes.md](./decisoes.md) para o índice completo dos 31 ADRs.
