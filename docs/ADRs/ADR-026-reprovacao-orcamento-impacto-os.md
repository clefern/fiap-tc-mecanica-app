# ADR-026: Reprovação de Orçamento e Impacto na Ordem de Serviço

## Status

Aceito

## Contexto

No fluxo atual da oficina, uma Ordem de Serviço (OS) segue o seguinte caminho principal:

- Abertura da OS em **RECEBIDA**.
- Diagnóstico em **EM_DIAGNOSTICO**.
- Emissão de orçamento, transicionando a OS para **AGUARDANDO_APROVACAO**.
- Aprovação do orçamento pelo cliente, que leva a OS para **APROVADA** (e depois **EM_EXECUCAO** etc.).

O domínio de **Orçamento** já suporta os seguintes estados:

- `GERADO` – orçamento emitido e aguardando decisão do cliente.
- `APROVADO` – orçamento aceito pelo cliente.
- `REJEITADO` – orçamento reprovado.
- `CANCELADO` – orçamento cancelado pelo sistema (por exemplo, ao gerar um novo orçamento ou cancelar a OS).

Também já existem eventos de domínio:

- `OrcamentoGeradoEvent` – usado para geração de PDF e envio de e-mail.
- `OrcamentoAprovadoEvent` – usado para atualizar a OS para **APROVADA**.
- `OrcamentoReprovadoEvent` – já publicado pelo serviço de orçamento, mas ainda sem listener dedicado.

A dúvida de negócio é: **quando o cliente reprova um orçamento (`REJEITADO`), o que deve acontecer com a OS?** As interpretações possíveis são:

1. Reprovação do orçamento implica **encerramento da OS** (por cancelamento).
2. Reprovação do orçamento **não encerra a OS**, permitindo ajustes e reemissão de um novo orçamento dentro da mesma OS.
3. Diferenciar explicitamente entre "reprovar definitivamente" e "solicitar ajustes", com comportamentos distintos para a OS.

Além disso, o domínio de OS já prevê um fluxo de retorno para diagnóstico:

- A partir de **AGUARDANDO_APROVACAO** é possível voltar para **EM_DIAGNOSTICO**, e o serviço de OS já cancela automaticamente orçamentos pendentes (`StatusOrcamento.GERADO`) nesse retorno.

Ou seja, o sistema já oferece um caminho técnico para **ajustes do orçamento** sem precisar atrelar essa intenção ao ato de "reprovar".

## Decisão

1. **Semântica clara para `reprovar orçamento`**

- O ato de chamar `Orcamento.reprovar()` (via endpoint `/api/orcamentos/{id}/reprovar`) representa a decisão de negócio de que **aquele orçamento foi rejeitado pelo cliente e o serviço não será executado com aquelas condições**.
- Essa ação é interpretada como **reprovação definitiva daquele orçamento**, não como pedido de ajuste.

2. **Impacto na OS: reprovação do orçamento encerra a OS via cancelamento**

- Quando um orçamento em estado `GERADO` é reprovado (`StatusOrcamento.REJEITADO`), a OS associada, que está em **AGUARDANDO_APROVACAO**, deve ser automaticamente transicionada para **CANCELADA**.
- Isso será implementado através de um listener para `OrcamentoReprovadoEvent`, que chamará `OrdemServicoService.cancelar(osId)`.
- O cancelamento da OS representa que, com base naquele orçamento, **o cliente optou por não seguir com o serviço**.

3. **Cenário de ajustes de orçamento: ação explícita separada**

- Para o cenário em que o cliente quer **ajustar o orçamento antes de aprovar**, não utilizaremos `reprovar`.
- Em vez disso, o fluxo de UI/API deve expor uma ação específica de **"retornar para diagnóstico"** da OS, reutilizando o caminho já existente:
  - OS em **AGUARDANDO_APROVACAO** volta para **EM_DIAGNOSTICO** via `OrdemServicoService.iniciarDiagnostico`.
  - Nesta transição, o serviço de OS já chama `orcamentoService.cancelarOrcamentosPendentes`, cancelando orçamentos `GERADO` associados.
- Assim, a semântica fica clara:
  - **Reprovar orçamento**: rejeição definitiva, OS é cancelada.
  - **Retornar para diagnóstico**: solicitação de ajuste, OS continua viva e um novo orçamento poderá ser gerado.

4. **Preservação de histórico do orçamento reprovado**

- Ao cancelar a OS por consequência de um orçamento reprovado, **não alteraremos o status `REJEITADO` desse orçamento para `CANCELADO`**.
- Isso permite rastrear claramente que a OS foi encerrada porque o cliente **rejeitou** o orçamento, e não por um cancelamento administrativo genérico.
- Para manter essa distinção, o listener que reage a `OrdemServicoCanceladaEvent` será ajustado para **somente cancelar orçamentos em estado `GERADO`**, não tocando em orçamentos `REJEITADO`.

## Consequências

### Positivas

- **Semântica clara de negócio**:
  - `REJEITADO` passa a significar explicitamente: "cliente não quer seguir com este orçamento".
  - Cancelar a OS a partir da reprovação reflete o processo real da oficina: sem orçamento aceito, não há serviço a executar.
- **Separação de intenções do cliente**:
  - Reprovar não é o mesmo que pedir ajuste; o fluxo de "retorno para diagnóstico" continua disponível separado.
- **Histórico preservado**:
  - Orçamentos reprovados continuam marcados como `REJEITADO`, mesmo que a OS seja cancelada depois.
  - Permite relatórios futuros (ex.: taxa de reprovação de orçamentos, motivos de perda, etc.).
- **Alinhamento com a arquitetura de eventos**:
  - A aprovação do orçamento já atualiza a OS via `OrcamentoAprovadoEvent` + listener.
  - A reprovação passa a ter o mesmo nível de importância e impacto, também orquestrada por evento.

### Negativas

- **Maior acoplamento conceitual entre orçamento e OS**:
  - Agora uma decisão no agregado `Orcamento` passa a dirigir diretamente uma transição crítica da OS (para `CANCELADA`).
  - Isso aumenta a complexidade mental ao depurar fluxos envolvendo cancelamentos.
- **Menos flexibilidade em cenários ambíguos**:
  - Se a UI/API usar o verbo "reprovar" quando, na verdade, o cliente quer apenas negociar, a OS será cancelada automaticamente.
  - Para evitar isso, é fundamental que o design de UX/API diferencie claramente "Reprovar" de "Solicitar ajustes".
- **Mais regras nos listeners**:
  - O listener de cancelamento de OS precisará ser consciente do estado do orçamento, para não sobrescrever `REJEITADO` com `CANCELADO`.

## Alternativas Consideradas

1. **Não cancelar a OS ao reprovar o orçamento**

- A OS permaneceria em **AGUARDANDO_APROVACAO** após a reprovação.
- Isso permitiria, em teoria, ajustes posteriores sem ações adicionais.
- Rejeitado porque:
  - O estado **AGUARDANDO_APROVACAO** passaria a ser ambíguo (aguardando primeira decisão ou pós-reprovação aguardando nova proposta?).
  - A equipe da oficina ficaria sem um sinal claro de que aquele atendimento foi efetivamente perdido.

2. **Criar um novo status de OS, por exemplo `REPROVADA`**

- A OS teria um estado distinto de `CANCELADA` para representar "perdemos a venda".
- Rejeitado por agora porque:
  - Introduz um novo estado em `StatusOS`, exigindo ajustes em todas as transições, relatórios, seeding e regras de segurança.
  - O estado `CANCELADA` já atende o cenário de OS encerrada sem execução, e a causa exata pode ser lida do status do orçamento associado (`REJEITADO` vs `CANCELADO`).

3. **Tratar reprovação como simples retorno para diagnóstico**

- Ao reprovar o orçamento, a OS voltaria para **EM_DIAGNOSTICO** automaticamente.
- Rejeitado porque:
  - Mistura duas intenções diferentes sob a mesma ação técnica.
  - Perde a capacidade de registrar explicitamente que um orçamento foi rejeitado (vs apenas ajustado).
  - O domínio já oferece um caminho explícito e controlado para retorno a diagnóstico.

## Ações de Implementação

1. **Listener para `OrcamentoReprovadoEvent`**

- Criar um listener dedicado que:
  - Leia o `Orcamento` do evento.
  - Identifique a OS associada (`ordemServicoId`).
  - Chame `OrdemServicoService.cancelar(osId)` para transicionar a OS para `CANCELADA`.

2. **Ajuste do listener de cancelamento da OS sobre orçamentos**

- Atualizar `AtualizacaoOrcamentoListener` para, ao reagir a `OrdemServicoCanceladaEvent`:
  - Localizar o orçamento relacionado.
  - Cancelar apenas orçamentos em estado `GERADO`.
  - Não alterar orçamentos já `REJEITADO`.

3. **Alinhamento de UX/API**

- Garantir que a camada de apresentação exponha claramente duas ações distintas ao cliente:
  - "Aprovar orçamento" (leva OS para **APROVADA**).
  - "Reprovar orçamento" (rejeita orçamento e cancela OS).
- Qualquer fluxo de "solicitar ajuste" deve usar explicitamente o caminho de retorno a diagnóstico, não o endpoint de reprovação.

## Data

2026-01-15
