# Debitos Tecnicos — Mecanica API

**Ultima atualizacao**: 2026-03-20 (verificado contra o codigo atual)
**Total analisado**: 283 classes Java + configuracoes

---

## Alta Prioridade (antes de producao)

| # | Categoria | Problema | Arquivo | Status |
|---|-----------|----------|---------|--------|
| ~~1~~ | ~~Seguranca~~ | ~~Stack trace exposto em respostas HTTP (`debug_message` em producao)~~ | ~~`GlobalExceptionHandler.java`~~ | Resolvido — removido `debug_message`; retorna mensagem generica com codigo `SYS-500` |
| ~~2~~ | ~~Seguranca~~ | ~~Emails de usuarios visiveis nos logs (PII em INFO/ERROR)~~ | ~~`SmtpNotificationService.java`~~ | Resolvido — logs agora usam `userId` em vez de email |
| ~~3~~ | ~~Seguranca~~ | ~~JWT secret com valor default hardcoded no yml~~ | ~~`application.yml`~~ | Resolvido — usa env var com default dev-only explicitamente marcado |
| ~~4~~ | ~~Seguranca~~ | ~~Autorizacao permissiva em `gerarPdf()`~~ | ~~`OrcamentoController.java`~~ | Resolvido — `@PreAuthorize` restringe a ATENDENTE+MECANICO+ADMIN |
| ~~5~~ | ~~BD~~ | ~~N+1 query em listagens de OrdemServico~~ | ~~`JpaOrdemServicoRepository.java`~~ | Resolvido — `@EntityGraph` em `findAll`, `findByStatus*` e filas; `@BatchSize(20)` na entity para native query |
| ~~6~~ | ~~BD~~ | ~~Indices faltando em ordens_servico e orcamentos~~ | ~~migrations~~ | Resolvido — `V16__add_indexes.sql` cobre todos |
| ~~7~~ | ~~BD~~ | ~~ON DELETE CASCADE em veiculo → cliente~~ | ~~`V1__initial_schema.sql`~~ | Resolvido — usa `ON DELETE RESTRICT` corretamente |
| ~~8~~ | ~~API~~ | ~~Paginacao ausente em Pecas e Insumos~~ | ~~controllers~~ | Resolvido — ambos usam `@PageableDefault` |
| ~~9~~ | ~~Arquitetura~~ | ~~Validacao de posse de veiculo no service~~ | ~~`OrdemServicoServiceImpl`~~ | Resolvido — extraido para `OsEntityValidator` |
| ~~10~~ | ~~Arquitetura~~ | ~~Cliente e Veiculo anemicos~~ | ~~`domain/model/`~~ | Resolvido — ambos tem metodos de negocio |
| 11 | DevOps | Credenciais em texto puro no `docker-compose.yml` (sem `.env`) | `docker-compose.yml` | Pendente (aceitavel em dev, mas deveria usar `.env`) |

**Resumo alta prioridade: 1 pendente de 11 originais (aceitavel em dev)**

---

## Media Prioridade

| # | Categoria | Problema | Status |
|---|-----------|----------|--------|
| ~~12~~ | ~~Seguranca~~ | ~~Rate limiting so no AuthController~~ | Aceitavel — infraestrutura preparada em `application.yml`, nao e bug |
| ~~13~~ | ~~Seguranca~~ | ~~DTOs sem @Valid~~ | Resolvido — 14 DTOs verificados com `@Valid` |
| ~~14~~ | ~~Erros~~ | ~~Ausencia de Correlation ID nos erros (sem MDC/tracing)~~ | Resolvido — `CorrelationIdFilter` com MDC, header `X-Correlation-ID` no response, correlation ID nas respostas de erro, log pattern atualizado |
| ~~15~~ | ~~Erros~~ | ~~`GlobalExceptionHandler` nao cobre `405` e `415`~~ | Resolvido — handlers para `HttpRequestMethodNotSupportedException` e `HttpMediaTypeNotSupportedException` adicionados |
| ~~16~~ | ~~Erros~~ | ~~Mistura PT/EN nas mensagens de erro~~ | Resolvido — todas em portugues consistentemente |
| ~~17~~ | ~~API~~ | ~~`listar()` de OrdemServico sem filtros~~ | Resolvido — `GET /api/ordens-servico` aceita `?status=X&clienteId=Y` (opcionais); Insomnia atualizado |
| 18 | API | Sem versionamento de API (`/api/v1/`) | Pendente (aceitavel para projeto academico) |
| ~~19~~ | ~~BD~~ | ~~Geracao de PDF dentro de transacao~~ | Resolvido — `recuperarPdf()` sem `@Transactional` |
| 20 | BD | Migrations sem documentacao de rollback | Pendente |
| 21 | Testes | Assercoes superficiais em controller tests (so `status()`, sem body) | Pendente |
| 22 | Testes | Falta de testes para casos de borda | Pendente |
| 23 | Testes | Setup duplicado em muitos testes (oportunidade de fixtures) | Pendente |
| ~~24~~ | ~~Codigo~~ | ~~Magic number: taxa de impostos (`0.05`) hardcoded~~ | Resolvido — extraido para `mecanica.orcamento.taxa-impostos` em `application.yml`, injetado via `@Value` |
| 25 | Codigo | `OrcamentoServiceImpl.gerarOrcamento()` com 50+ linhas | Pendente |
| ~~26~~ | ~~Codigo~~ | ~~Exists check duplicado~~ | Aceitavel — padrao apropriado para validacao de duplicatas |
| 27 | Performance | `@Cacheable` ausente em listagens estaticas | Pendente |
| 28 | Performance | PDF gerado de forma sincrona (bloqueia thread) | Pendente |
| 29 | Docs | Endpoints sem exemplos de request/response no Swagger | Pendente |

**Resumo media prioridade: 7 pendentes de 18 originais**

---

## Baixa Prioridade

| # | Categoria | Problema | Status |
|---|-----------|----------|--------|
| 30 | Arquitetura | `List<ItemOrdemServico>` exposta via getter publico no agregado | Pendente |
| 31 | Testes | Ausencia de testes de autorizacao negativa (ATENDENTE → 403) | Pendente |
| 32 | Testes | Testes E2E acoplados a dados de seed | Pendente |
| 33 | Performance | Sem health check customizado para SMTP | Pendente |
| 34 | Performance | Sem tracing distribuido (OpenTelemetry) | Pendente |
| 35 | Codigo | `TODO`/comentarios desnecessarios em codigo de producao | Pendente |
| 36 | DevOps | Makefile sem target `clean-all` | Pendente |

---

## Refactors Planejados (OrdemServicoServiceImpl)

| ID | Descricao | Status |
|----|-----------|--------|
| ~~REFACTOR-OS-1~~ | ~~Extrair `OsEstoqueValidator`~~ | Resolvido — componente criado |
| ~~REFACTOR-OS-2~~ | ~~Extrair `OsMecanicoAssigner`~~ | Resolvido — componente criado |
| ~~REFACTOR-OS-3~~ | ~~Extrair `OsEntityValidator`~~ | Resolvido — componente criado |
| REFACTOR-OS-4 | Criar `OsItemService`: extrair `adicionarItem`, `adicionarItensEmLote`, `atualizarQuantidadeItem`, `removerItem` | Pendente |

---

## Ideias Futuras (Backlog)

| Ideia | Contexto |
|-------|----------|
| Mecanico por item de servico | Saber qual mecanico trabalhou em cada item da OS |
| Evento ao mudar prioridade | Quando OS se torna URGENTE, disparar Domain Event (padrao ADR-014) |
| Prioridade de execucao por item | Campo `prioridade_execucao` em `itens_ordem_servico` |
| Role de Gerente | Avaliar role intermediaria entre Admin e Atendente |
| SLO/SLA operacionais | Metas de tempo (ex: "enviar orcamento em ate 2s, 95% do tempo") |
| Dashboard Grafana | Painel com metricas-chave do sistema |
| Alerta de estoque na OS | Email para admin quando pecas insuficientes; setar OS para BAIXA |
| SMTP real (producao) | Guia de configuracao para Gmail, SendGrid ou AWS SES |

---

## Resumo Atualizado

| Prioridade | Total Original | Resolvidos | Pendentes |
|------------|---------------|------------|-----------|
| Alta | 11 | 10 | **1** (aceitavel) |
| Media | 18 | 11 | **7** |
| Baixa | 7 | 0 | **7** |
| Refactors | 4 | 3 | **1** |
| **Total** | **40** | **24** | **16** |
