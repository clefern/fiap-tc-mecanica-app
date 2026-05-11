# ADR-014: Gestão Automática de Orçamentos via Domain Events

## Status

Aceito

## Contexto

Atualmente, o processo de criação de uma Ordem de Serviço (OS) e a subsequente geração de orçamento estão acoplados. Quando uma OS é encaminhada para orçamento, o sistema precisa calcular valores de peças e mão de obra, gerar um documento (PDF) e persistir o orçamento.

Fazer tudo isso de forma síncrona e acoplada dentro do `OrdemServicoService` viola o Princípio de Responsabilidade Única (SRP) e dificulta a manutenção e testes. Além disso, queremos preparar o sistema para um futuro onde a aprovação ou geração de orçamento possa disparar outras ações (notificações, integrações, etc.) sem modificar o código core da OS.

## Decisão

Decidimos utilizar **Domain Events** (Eventos de Domínio) intra-processo utilizando o mecanismo de eventos do Spring Framework (`ApplicationEventPublisher` e `@EventListener`).

O fluxo será:

1.  O `OrdemServicoService` finaliza a preparação da OS e publica um evento `OrdemServicoAguardandoAprovacaoEvent`.
2.  Um listener dedicado, `GeracaoOrcamentoListener`, escuta esse evento.
3.  O listener orquestra a chamada ao `OrcamentoService` para realizar os cálculos, gerar o PDF e persistir o orçamento.

### Componentes Envolvidos

- **Evento**: `OrdemServicoAguardandoAprovacaoEvent` (contém a referência da OS).
- **Publicador**: `OrdemServicoServiceImpl` (ao chamar `emitirOrcamento`).
- **Ouvinte**: `GeracaoOrcamentoListener`.
- **Serviço de Domínio**: `OrcamentoService` (contém a lógica de negócio do orçamento).

## Consequências

### Positivas

- **Desacoplamento**: O serviço de OS não precisa saber _como_ o orçamento é gerado, apenas que o processo chegou nessa etapa.
- **Extensibilidade**: Novos listeners podem ser adicionados para o mesmo evento (ex: `NotificacaoClienteListener`) sem alterar o código existente.
- **Testabilidade**: Podemos testar a lógica de OS verificando apenas se o evento foi disparado, e testar a geração de orçamento isoladamente no listener/serviço correspondente.
- **Organização**: Melhor separação de responsabilidades (SRP).

### Negativas

- **Complexidade Indireta**: O fluxo de execução não é linear/sequencial no mesmo método, exigindo navegação pelo código para entender quem escuta o evento.
- **Rastreabilidade**: Em caso de falhas no listener, o tratamento de erro precisa ser cuidadoso para garantir que o estado do sistema permaneça consistente (embora, neste caso, estejamos usando eventos síncronos por padrão do Spring, o que mantém a transação se configurado corretamente, ou requer atenção se for assíncrono). _Nota: Inicialmente implementado de forma síncrona._

## Alternativas Consideradas

- **Chamada Direta**: Manter a chamada `orcamentoService.gerarOrcamento(os)` dentro de `OrdemServicoService`. Rejeitado por manter alto acoplamento.
- **Mensageria Externa (RabbitMQ/Kafka)**: Exagerado (Overengineering) para o momento atual, já que ambos os contextos estão no mesmo monólito modular.

## Data

2026-01-11
