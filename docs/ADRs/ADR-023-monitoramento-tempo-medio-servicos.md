# ADR-023: Monitoramento do Tempo Médio de Execução dos Serviços

**Status**: Proposta
**Data**: 2026-01-15
**Relacionado a**: [ADR-009-implementacao-sonarqube.md](ADR-009-implementacao-sonarqube.md), [ADR-018-estrategia-testes-e2e.md](ADR-018-estrategia-testes-e2e.md)

## 1. Contexto e Problema

O sistema da mecânica vem evoluindo em complexidade, com múltiplos fluxos de negócio (abertura de OS, diagnóstico, orçamento, aprovação, execução, faturamento, envio de email etc.) e diversos serviços de aplicação e de domínio orquestrando essas operações.

Embora já existam logs estruturados e algumas métricas técnicas (ex.: saúde da aplicação, erros, cobertura de testes), ainda **não há visibilidade sistemática sobre o tempo médio de execução dos serviços**. Isso gera alguns problemas:

- Dificuldade em identificar gargalos de performance em cenários reais de uso.
- Ausência de indicadores objetivos para priorizar melhorias técnicas e de UX.
- Impossibilidade de definir SLOs/SLAs claros para operações críticas (ex.: geração de orçamento, envio de email com PDF, atualização de estoque).

É necessário estabelecer uma estratégia padronizada para **medir, armazenar e visualizar o tempo médio de execução de serviços** relevantes, de forma alinhada com boas práticas de observabilidade e sem acoplamento excessivo ao domínio.

## 2. Decisão

Decidimos implementar um **mecanismo de monitoramento de tempo de execução de serviços** baseado em:

1. **Métricas técnicas via Micrometer/Actuator** para exposição de tempos médios (e percentis) em um endpoint padrão de observabilidade (`/actuator/metrics`).
2. **Anotações/AOP** em serviços de aplicação e integrações críticas para capturar automaticamente a duração das operações.
3. **Coleta por ferramentas externas** (Prometheus, Grafana etc.) para visualização de dashboards e alertas.

### 2.1. Endpoint Específico de Tempo Médio?

Não será criado um **endpoint REST de negócio dedicado** apenas para retornar o tempo médio dos serviços (ex.: `GET /api/monitor/tempo-medio`). Em vez disso:

- Utilizaremos o **endpoint padrão de métricas do Spring Boot Actuator** (`/actuator/metrics`) para expor as métricas de tempo.
- Caso seja necessário para relatórios gerenciais, um endpoint de leitura poderá ser discutido em uma ADR futura, mas inicialmente **não fará parte da superfície pública da API de domínio**.

Motivações para não expor via endpoint de domínio:

- Evitar acoplamento entre **camada de negócio** e **detalhes de observabilidade**.
- Manter a superfície de API limpa para clientes externos (front, integrações) e evitar uso indevido dessas métricas como "fonte de verdade" para contratos de negócio.
- Aproveitar padrões consolidados de observabilidade (Prometheus/Grafana) em vez de reinventar mecanismos de consulta.

## 3. Estrutura da Solução

### 3.1. Abordagem Técnica

1. **Micrometer + Actuator**
   - Habilitar o Actuator e expor ao menos o endpoint `/actuator/metrics` no perfil `dev` (e outros ambientes internos).
   - Utilizar `Timer`/`LongTaskTimer` do Micrometer para registrar a duração dos serviços.

2. **AOP/Anotação para Medição**
   - Criar uma anotação, por exemplo `@MonitoredOperation("nome.servico")`, aplicável em métodos de serviços de aplicação.
   - Implementar um *aspect* que:
     - Inicie um `Timer.Sample` antes da execução do método.
     - Registre a duração ao final, incluindo rótulos (service, operation, status).
     - Garanta a publicação mesmo em caso de exceção (usando bloco `finally`).

3. **Nomenclatura de Métricas**
   - Padrão de nome: `mecanica.service.execution`.
   - *Tags* mínimas:
     - `service`: nome lógico do serviço (ex.: `OrcamentoService`).
     - `operation`: operação específica (ex.: `gerarOrcamento`, `aprovarOrcamento`).
     - `status`: `success` ou `error`.

### 3.2. Escopo Inicial (MVP)

Como MVP, serão monitoradas as operações mais críticas:

1. `OrcamentoServiceImpl.gerarOrcamento`
2. `OrcamentoServiceImpl.aprovar`
3. `OrdemServicoServiceImpl.iniciarDiagnostico`
4. `OrdemServicoServiceImpl.iniciarExecucao`
5. `EnvioEmailService` (geração e envio de PDF)

Esses pontos de medição já fornecem boa visibilidade sobre:

- Latência na geração e aprovação de orçamento.
- Tempo de resposta para início de atividades de diagnóstico e execução.
- Impacto do envio de emails (PDF) no tempo de resposta.

### 3.3. Exposição e Consumo das Métricas

1. As métricas serão expostas via Actuator em endpoints como:
   - `GET /actuator/metrics/mecanica.service.execution`
2. Ferramentas de monitoramento (ex.: Prometheus) poderão coletar esses dados periodicamente.
3. Dashboards em Grafana (ou equivalente) poderão exibir:
   - Tempo médio (mean) por serviço/operação.
   - Percentis (p95, p99) por serviço/operação.
   - Taxa de erro por operação.

## 4. Consequências

### 4.1. Positivas

- **Observabilidade Real**: Permite identificar serviços mais lentos e orientar otimizações.
- **Base para SLO/SLAs**: Com métricas consolidadas, é possível negociar e monitorar objetivos de desempenho.
- **Baixo Acoplamento**: Observabilidade fica isolada em infraestrutura/AOP, sem poluir o domínio.
- **Aderência a Padrões**: Usa stack padrão (Micrometer/Actuator) e integra facilmente com ferramentas de mercado.

### 4.2. Negativas

- **Complexidade Adicional**: Introduz AOP e novas dependências (Actuator/Micrometer), exigindo cuidado em configuração.
- **Sobrecarga de Execução**: Cada medição adiciona pequena sobrecarga; deve ser monitorada, mas é geralmente aceitável.
- **Gestão de Acesso**: Endpoints de métricas devem ser protegidos (autenticação/autorização) em ambientes não-dev.

## 5. Plano de Implementação

1. **Configuração Básica**
   - Habilitar Spring Boot Actuator e Micrometer no projeto.
   - Configurar exposição de `/actuator/metrics` para ambientes `dev` e `test`.

2. **Infraestrutura de Medição**
   - Criar anotação `@MonitoredOperation` no módulo de infraestrutura.
   - Implementar *aspect* para registrar tempos usando `Timer` do Micrometer.

3. **Aplicação em Serviços Críticos**
   - Anotar métodos prioritários em `OrcamentoServiceImpl`, `OrdemServicoServiceImpl` e serviços de email.

4. **Validação e Testes**
   - Criar testes de integração garantindo que, após chamadas aos serviços, as métricas apareçam em `/actuator/metrics`.
   - Validar que exceções ainda registram tempos com `status=error`.

5. **Fase Futuras (Opcional)**
   - Adicionar métricas em outros serviços de alto impacto.
   - Configurar dashboards em ferramenta de observabilidade (Grafana, por exemplo).
   - Avaliar necessidade de endpoint de leitura específico para relatórios de negócio (se demandado pela gestão), em nova ADR.

