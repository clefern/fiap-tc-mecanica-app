# ADR-024: Endpoint de Monitoramento do Tempo Médio de Execução das OSs

**Status**: Aprovada
**Data**: 2026-01-15
**Relacionado a**: [ADR-020-subdominio-prioridade-os.md](ADR-020-subdominio-prioridade-os.md), [ADR-023-monitoramento-tempo-medio-servicos.md](ADR-023-monitoramento-tempo-medio-servicos.md)

## 1. Contexto e Problema

O Tech Challenge da pós-graduação exige explicitamente um **endpoint de monitoramento** que exponha o:

- **Tempo médio de execução das Ordens de Serviço (OSs)**, definido como o intervalo entre o momento em que a OS é colocada em **`APROVADA`** e o momento em que chega em **`FINALIZADA`**.

Além disso, há uma oportunidade de ir além do requisito mínimo e **medir quanto tempo as OSs permanecem em cada status**, fornecendo uma visão mais rica do fluxo (lead time por etapa), ainda que isso não seja uma exigência formal do desafio.

Hoje já temos:

- **Observabilidade técnica** via Spring Actuator + Micrometer, conforme [ADR-023](ADR-023-monitoramento-tempo-medio-servicos.md).
- Campos de domínio em `OrdemServico` que registram momentos importantes:
  - `dataEntrada` (quando a OS é recebida).
  - `dataAprovacao` (quando o orçamento é aprovado e a OS entra em `APROVADA`).
  - `dataFechamento` (quando a OS é finalizada).

Porém, **a API ainda não expõe um endpoint funcional** que consolide essa informação para consumo externo (relatórios, dashboards, requisito acadêmico).

## 2. Decisão

Decidimos:

1. **Expor um endpoint REST funcional** específico para monitoramento do tempo médio de execução das OSs, utilizando os dados de domínio persistidos (datas) como fonte de verdade.
2. **Complementar** a observabilidade técnica já existente, sem substituí-la:
   - As métricas Micrometer/Actuator continuam sendo usadas para latência de serviços.
   - O novo endpoint foca em **métricas de negócio** (tempo de ciclo da OS).
3. **Planejar a capacidade de extensão** para, futuramente, expor o tempo médio em cada status, sem acoplar essa complexidade agora.

### 2.1. Endpoint Obrigatório (Tech Challenge)

Será criado e mantido um endpoint REST para expor o tempo médio de execução das OSs. Na implementação atual, ele está integrado ao contexto de relatórios gerenciais:

- **`GET /api/relatorios/tempo-medio-execucao-os`**

Comportamento esperado:

- Considerar apenas OSs em status **`FINALIZADA`** e que possuam `dataAprovacao` e `dataFechamento` preenchidas.
- Calcular o tempo de execução por OS como: `dataFechamento - dataAprovacao`.
- Retornar a **média de tempo de execução** entre todas as OSs elegíveis.

Resposta (contrato de alto nível atual):

```json
{
  "id": null,
  "geradoEm": "2026-01-15T10:30:00",
  "quantidadeOsConsideradas": 42,
  "tempoMedioExecucao": "PT2H16M",
  "tempoMinimoExecucao": "PT0H45M",
  "tempoMaximoExecucao": "PT5H30M"
}
```

Observações:

- Os campos de duração seguem o padrão ISO-8601 (`java.time.Duration`).
- O campo `id` será utilizado quando o relatório for persistido como snapshot histórico.
- O campo `geradoEm` indica a data/hora em que a métrica foi calculada.

### 2.2. Endpoint de Período Observável

Além do endpoint agregado globalmente, foi decidido expor um segundo endpoint, focado em análise por janela de tempo observável:

- `GET /api/relatorios/tempo-medio-execucao-os/periodo`

Comportamento esperado:

- Receber obrigatoriamente os parâmetros de query `inicio` e `fim` no formato `AAAA-MM-DD`.
- Validar que `inicio` não é posterior a `fim`, retornando erro de validação (400) em caso de violação.
- Considerar apenas OSs em status `FINALIZADA` com `dataAprovacao` e `dataFechamento` preenchidas cujo `dataFechamento` esteja dentro do intervalo `[inicio, fim]` (inclusivo).
- Reutilizar o mesmo contrato de resposta de `TempoMedioExecucaoOs` descrito na Seção 2.1.

Esse endpoint é direcionado a painéis analíticos e relatórios gerenciais que precisam comparar diferentes janelas de tempo (por exemplo, últimas 24h, últimos 7 dias, último mês), enquanto o endpoint global continua servindo como visão consolidada histórica.

### 2.3. Extensões Futuras (Tempo por Status)

Embora não seja requisito do desafio, planejamos a possibilidade de expor, futuramente:

- Tempo médio que as OSs ficam em cada status (`RECEBIDA`, `EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO`, `APROVADA`, `EM_EXECUCAO`, `FINALIZADA`, `ENTREGUE`).

Para isso, será necessária uma estratégia adicional de modelagem (ver Seção 3.3), mas **não será implementada neste primeiro ciclo**.

## 3. Estrutura da Solução

### 3.1. Modelagem de Dados (Estado Atual)

O agregado `OrdemServico` já contém campos suficientes para o requisito mínimo:

- `dataAprovacao`: preenchida quando a OS é aprovada (status `APROVADA`).
- `dataFechamento`: preenchida quando a OS é finalizada (status `FINALIZADA`).

Com base nisso, o tempo de execução de uma OS é calculado como:

```text
tempoExecucao = dataFechamento - dataAprovacao
```

Regras:

1. OSs sem `dataAprovacao` ou sem `dataFechamento` são **ignoradas** no cálculo.
2. OSs que eventualmente forem reabertas ou canceladas após `FINALIZADA` devem ser tratadas conforme regra de negócio (a princípio, cenário raro e fora do escopo deste requisito).

### 3.2. Serviço de Monitoramento

O cálculo do tempo médio de execução das OSs foi integrado ao contexto de relatórios, reutilizando o serviço já existente:

- `RelatorioService` / `RelatorioServiceImpl` na camada de aplicação.

Responsabilidades principais:

1. Consultar o repositório de relatórios (`RelatorioRepository`) que, por sua vez, utiliza `JpaOrdemServicoRepository` para obter as OSs finalizadas com `dataAprovacao` e `dataFechamento` válidas.
2. Calcular, em memória, a média de tempo de execução utilizando `Duration.between(dataAprovacao, dataFechamento)`.
3. Retornar um objeto de domínio de leitura `TempoMedioExecucaoOs` com campos ricos para decisão.

### 3.3. Repositório e Consultas

Para atender ao requisito com eficiência, utilizamos o repositório de relatórios (`RelatorioRepository`) em conjunto com o repositório JPA de OS (`JpaOrdemServicoRepository`), que expõe um método específico:

- `List<OrdemServicoEntity> findByStatusAndDataAprovacaoIsNotNullAndDataFechamentoIsNotNull(StatusOS status);`
  - Critério: `status = FINALIZADA` e `dataAprovacao` e `dataFechamento` não nulas.

O cálculo da média, bem como dos tempos mínimo e máximo, é feito em memória. Caso o volume de dados cresça significativamente, poderemos introduzir consultas agregadas no banco de dados em ADR futura.

### 3.4. Endpoint REST

O endpoint foi integrado ao `RelatorioController`, alinhado ao contexto de relatórios gerenciais:

- `GET /api/relatorios/tempo-medio-execucao-os`

Responsabilidades do controller:

1. Delegar para `RelatorioService` o cálculo da métrica.
2. Retornar diretamente o objeto de domínio `TempoMedioExecucaoOs` como resposta JSON.
3. Utilizar as anotações de documentação (`@Operation`, `@ApiResponses`) e segurança já padronizadas no projeto (role `ADMIN`).

Além disso, o método de serviço é anotado com `@MonitoredOperation("relatorio.tempoMedioExecucaoOs")` para integrar-se à infraestrutura de observabilidade já existente.

### 3.5. Domínio de Relatório Enriquecido

O domínio de relatório `TempoMedioExecucaoOs` foi modelado com campos adicionais para oferecer mais insumos de decisão ao usuário:

- `id`: identificador opcional do relatório, usado quando um snapshot for persistido.
- `geradoEm`: data/hora em que o relatório foi calculado.
- `quantidadeOsConsideradas`: quantidade de OSs efetivamente utilizadas no cálculo.
- `tempoMedioExecucao`: duração média entre `APROVADA` e `FINALIZADA`.
- `tempoMinimoExecucao`: menor duração entre `APROVADA` e `FINALIZADA` dentre as OSs consideradas.
- `tempoMaximoExecucao`: maior duração entre `APROVADA` e `FINALIZADA` dentre as OSs consideradas.

Esses campos permitem ao usuário avaliar rapidamente dispersão (mínimo/máximo), representatividade (`quantidadeOsConsideradas`) e momento de geração (`geradoEm`).

### 3.6. Snapshots Manuais para Comparação Futura (Planejado)

Para possibilitar comparações históricas (por exemplo, acompanhar a evolução mensal do tempo médio), será adotada a seguinte estratégia em uma próxima iteração:

1. **Disparo manual**: um endpoint dedicado (provavelmente `POST /api/relatorios/tempo-medio-execucao-os/snapshot`) permitirá que um usuário autorizado gere e persista um snapshot do relatório.
2. **Persistência**: o snapshot será salvo em uma tabela/entidade específica (ex.: `tempo_medio_execucao_os`), contendo ao menos os campos de `TempoMedioExecucaoOs` (`id`, `geradoEm`, `quantidadeOsConsideradas`, `tempoMedioExecucao`, `tempoMinimoExecucao`, `tempoMaximoExecucao`).
3. **Comparação futura**: endpoints de consulta poderão listar snapshots anteriores (por período, por exemplo) para comparação de desempenho ao longo do tempo.

Nesta ADR, apenas a modelagem de domínio e a presença dos campos necessários foram consolidadas. A implementação da persistência e dos endpoints de snapshot será formalizada em ADR específica quando for priorizada.

### 3.5. Integração com Observabilidade Técnica

A decisão desta ADR **não substitui** a estratégia de observabilidade técnica descrita em [ADR-023](ADR-023-monitoramento-tempo-medio-servicos.md). Em vez disso:

1. O endpoint de monitoramento de OS utiliza **dados de negócio** (datas da OS) como fonte de verdade para o tempo de execução.
2. Opcionalmente, o próprio serviço de monitoramento pode atualizar uma métrica de observabilidade, por exemplo:

   - `mecanica.business.os.execution-time.avg`

   Isso permitiria ver, em Prometheus/Grafana, a evolução histórica da média calculada periodicamente.

Essa integração, porém, é um plus e não um requisito do desafio.

## 4. Extensões Futuras: Tempo Médio em Cada Status

Para medir o tempo médio que as OSs passam em cada status, existem duas abordagens principais:

1. **Campos adicionais na entidade `OrdemServico`**
   - Ex.: `dataInicioDiagnostico`, `dataInicioExecucao`, etc.
   - Simples, mas aumenta o acoplamento da entidade à linha do tempo detalhada.

2. **Tabela/Entidade de Histórico de Status (Audit Trail)**
   - Ex.: `HistoricoStatusOS` com campos:
     - `osId`
     - `statusAnterior`
     - `statusNovo`
     - `dataMudanca`
   - Permite reconstruir a linha do tempo de cada OS e calcular tempos médios por status.

Esta ADR **não exige** a implementação dessa capacidade, mas recomenda que, se o requisito surgir, a segunda abordagem (histórico) seja preferida por ser mais flexível e alinhada com auditoria e relatórios futuros.

## 5. Consequências

### 5.1. Positivas

- **Atendimento ao requisito do Tech Challenge**: Passamos a expor explicitamente o tempo médio de execução das OSs via endpoint REST.
- **Métrica de Negócio Clara**: Tempo de ciclo entre `APROVADA` e `FINALIZADA` torna-se um indicador oficial de eficiência operacional.
- **Integração com Observabilidade**: Podemos correlacionar o tempo de ciclo de negócio com métricas técnicas (latência de serviços, uso de CPU, etc.).
- **Base para Evolução**: Abre caminho para monitorar tempos por status, gargalos de fila e otimizações de fluxo.

### 5.2. Negativas

- **Complexidade de Relatórios**: Dependendo do volume de OSs, consultas agregadas podem exigir otimização (índices, cache, etc.).
- **Interpretação do Indicador**: O tempo médio pode ser influenciado por outliers (OSs muito antigas ou travadas). Será necessário clareza na documentação.
- **Possível Divergência**: Métrica calculada via datas de negócio pode não coincidir com medições técnicas (Micrometer), o que exige cuidado na comunicação.

## 6. Plano de Implementação

1. **Repositório**
   - Adicionar método(s) em `OrdemServicoRepository` / `JpaOrdemServicoRepository` para recuperar OSs finalizadas com `dataAprovacao` e `dataFechamento`.

2. **Serviço de Aplicação**
   - Criar `MonitoramentoOsService` com método para calcular o tempo médio de execução das OSs.
   - Implementar lógica de cálculo usando `Duration.between(dataAprovacao, dataFechamento)`.

3. **DTO e Controller**
   - Criar DTO de resposta (`TempoMedioExecucaoOsResponse`).
   - Implementar `MonitoramentoOsController` ou estender `RelatorioController` com o endpoint:
     - `GET /api/monitoramento/os/tempo-medio-execucao`.
   - Documentar com OpenAPI (`@Operation`, `@ApiResponses`).

4. **Testes**
   - Criar testes de integração cobrindo:
     - Cálculo correto da média com múltiplas OSs finalizadas.
     - Comportamento quando não há OSs elegíveis (ex.: retorno com média 0 ou estrutura vazia, conforme regra definida).
   - Validar que o endpoint respeita as configurações de segurança (ex.: apenas perfis autorizados podem consultar).

5. **Evolução (Opcional)**

- Planejar, em ADR futura, a modelagem de histórico de status caso o requisito de tempo médio por status seja formalizado.
- Planejar, em ADR futura, a implementação de persistência de snapshots manuais conforme Seção 3.6.
