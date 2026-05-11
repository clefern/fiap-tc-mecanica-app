# ADR-003: Adoção de API Orientada a Tarefas para Transição de Status da OS

**Status:** Aceito
**Data:** 08/01/2026
**Autores:** Time de Arquitetura
**Contexto:** Análise detalhada em `docs/architecture/os-status-transition-analysis.md`

## Contexto
O sistema de gerenciamento de oficina mecânica possui um fluxo de vida complexo para a Ordem de Serviço (OS), envolvendo estados como `RECEBIDA`, `EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO`, `APROVADA`, `EM_EXECUCAO`, `FINALIZADA` e `ENTREGUE`.
Atualmente, a transição entre esses estados é realizada através de um endpoint genérico `PATCH /api/ordens-servico/{id}/status`, que aceita o novo status como parâmetro.

**Problemas identificados:**
1.  **Violação de Requisito:** O documento de requisitos exige "Alteração automática dos status conforme ações no sistema". O modelo atual delega a decisão do status para o cliente da API.
2.  **Segurança Frágil:** É difícil aplicar controles de acesso granulares (ex: "apenas o Cliente pode aprovar") em um único endpoint genérico.
3.  **Falta de Intenção:** O log de auditoria registra apenas "mudança de estado", perdendo o contexto de negócio (ex: "Aprovação de Orçamento").
4.  **Risco Operacional:** Consumidores da API (especialmente sem frontend guiado) podem tentar transições inválidas, dependendo excessivamente de validações defensivas no backend.

## Decisão
Decidimos **abandonar o modelo CRUD/PATCH** para transições de estado da OS e **adotar uma API Orientada a Tarefas (Task-Based API)**.

Em vez de expor "dados" (o campo status) para serem modificados, exporemos "ações" de negócio.

### Novos Endpoints (Ações)
Os seguintes endpoints substituirão a funcionalidade de alteração manual de status:

*   `POST /os/{id}/acoes/iniciar-diagnostico` -> Transita para `EM_DIAGNOSTICO`
*   `POST /os/{id}/acoes/emitir-orcamento` -> Transita para `AGUARDANDO_APROVACAO`
*   `POST /os/{id}/acoes/aprovar` -> Transita para `APROVADA` (Apenas Role: CLIENTE)
*   `POST /os/{id}/acoes/iniciar-execucao` -> Transita para `EM_EXECUCAO`
*   `POST /os/{id}/acoes/finalizar` -> Transita para `FINALIZADA`
*   `POST /os/{id}/acoes/entregar` -> Transita para `ENTREGUE`
*   `POST /os/{id}/acoes/cancelar` -> Transita para `CANCELADA`

## Consequências

### Positivas
*   **Segurança por Design:** Cada ação tem seu próprio endpoint, permitindo anotações `@PreAuthorize` específicas por Role.
*   **Conformidade:** Atende ao requisito de "alteração automática" de status.
*   **Robustez:** Elimina a possibilidade de o consumidor enviar um status inexistente ou pular etapas proibidas.
*   **Expressividade:** O código e a API refletem a linguagem ubíqua do domínio (Ubiquitous Language).

### Negativas
*   **Aumento da Superfície da API:** Teremos mais endpoints para documentar e manter em comparação a um único endpoint genérico.
*   **Refatoração Necessária:** Requer alteração nos testes existentes que dependiam do `PATCH /status`.

## Estratégia de Migração
1.  Implementar novos endpoints.
2.  Deprecar o endpoint `PATCH /status` (remover da documentação pública, manter apenas para admin/suporte temporariamente).
3.  Atualizar documentação do Insomnia.
