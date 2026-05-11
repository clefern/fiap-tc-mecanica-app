# ADR-029: Estratégia de Testes de Carga e de Interface

## Status
Aceito

## Contexto

O projeto Mecânica API já possui decisões consolidadas para testes unitários, integrados e end-to-end (ver ADR-018 e ADR-027), além de uma documentação geral de qualidade de testes. No entanto, ainda não há uma diretriz formal para:

1. Testar o comportamento do sistema sob carga (volume de requisições simultâneas, picos de uso e cenários de estresse).
2. Testar a interface do usuário (quando existir uma UI dedicada) de forma automatizada, garantindo regressão visual e funcional das principais jornadas.

Problemas identificados:

- Ausência de métricas objetivas de performance (latência, throughput, taxa de erro) para fluxos críticos como criação/execução de OS, aprovação de orçamento e relatórios.
- Risco de regressões de UX quando uma interface gráfica for adicionada, caso a validação permaneça apenas manual.
- Dificuldade em reproduzir e comparar comportamentos sob carga entre diferentes versões da aplicação.

Este ADR define a estratégia e o stack recomendados para **testes de carga** e **testes de interface**, complementando a pirâmide de testes já estabelecida.

## Decisão

Adotaremos duas frentes complementares:

1. Padronizar **testes de carga** com **k6** focados em fluxos de negócio críticos.
2. Padronizar **testes de interface** com **Playwright** para validar a experiência do usuário em uma futura UI web que consuma a Mecânica API.

### 1. Testes de Carga com k6

Decidimos utilizar o **k6** como ferramenta padrão de testes de carga para a Mecânica API.

Critérios que levaram à escolha:

- Scriptável em JavaScript, com curva de aprendizado baixa para o time.
- Fácil versionamento junto ao código da aplicação (scripts em repositório Git).
- Integração simples com containers e CI (execução em linha de comando ou via Docker).
- Boas capacidades de métricas (latência P95/P99, taxa de erro, RPS) e possibilidade de exportar resultados.

Escopo inicial dos cenários de carga:

1. Autenticação (`/oauth/token`) para usuários Atendente, Mecânico e Admin.
2. Fluxo de criação de OS até aprovação de orçamento:
   - Criação de cliente/veículo.
   - Criação de OS.
   - Inclusão de itens (peças, insumos, serviços).
   - Emissão e aprovação de orçamento.
3. Fila de prioridade e execução de OS:
   - Consulta de filas de orçamento e execução.
   - Início e finalização de execução.
4. Relatórios de tempo médio de execução de OSs.

Diretrizes de uso:

- Scripts de carga devem ficar em um diretório separado, por exemplo `load-tests/`, na raiz do repositório.
- Cada script deve representar um **cenário de negócio** com ramp-up e ramp-down configurados (por exemplo, aumentar de 1 para 50 usuários virtuais em X segundos).
- As credenciais usadas em testes de carga devem ser **usuários de teste** em um ambiente dedicado (ex: `staging`), jamais usuários reais.
- Testes de carga não devem ser executados automaticamente em cada commit; a recomendação é integrá-los como:
  - Job manual na pipeline (`on demand`) para validação antes de releases relevantes.
  - Job agendado (ex: diário) em ambiente de homologação, se os recursos permitirem.

Métricas mínimas a serem coletadas e acompanhadas:

- Latência P95 por endpoint alvo.
- Throughput (requests/segundo) sustentado sem degradação significativa.
- Taxa de erros HTTP (4xx/5xx) durante o cenário.
- Eventuais quedas de instâncias ou falhas de conexão.

### 2. Testes de Interface com Playwright

Para testes de interface de usuário (UI) – quando uma aplicação web for disponibilizada para consumir a Mecânica API – adotaremos **Playwright** como ferramenta padrão.

Justificativas:

- Suporte a múltiplos navegadores e contexto moderno de aplicações web (SPA/MPA).
- API expressiva para cenários end-to-end de UI (interação com formulários, validação visual básica, navegação entre páginas).
- Boa integração com CI (inclusive rodando em modo headless).

Escopo dos testes de interface:

- Journeys críticas vistas pelo usuário final, por exemplo:
  - Atendente registrando cliente e veículo.
  - Mecânico criando e orçando uma OS.
  - Cliente aprovando/reprovando orçamento e consultando o status.
  - Admin consultando relatórios gerenciais.
- Verificações focadas em:
  - Presença e habilitação de elementos de UI relevantes.
  - Fluxos de navegação entre telas.
  - Comportamento correto frente a erros de validação retornados pela API.

Diretrizes de uso:

- Os testes de interface devem residir em um projeto próprio (por exemplo, `ui-tests/` ou dentro do repositório da UI quando esta existir), mantendo a Mecânica API isolada como backend.
- Testes de interface **não substituem** os testes E2E de API; eles se somam para validar a experiência completa do usuário.
- Em CI, os testes de interface devem rodar em uma etapa dedicada, após a subida de um ambiente de teste com backend disponível.

## Tecnologias Avaliadas

### Testes de Carga

| Tecnologia | Veredito | Justificativa |
| :--- | :--- | :--- |
| JMeter | Rejeitado | GUI pesada, scripts difíceis de versionar e revisar em code review; menos amigável para automação moderna em comparação ao k6. |
| Gatling | Rejeitado | Forte, mas baseado em Scala; maior barreira de entrada para o time atual. |
| k6 | Escolhido | Scripts em JS, fácil integração com Docker/CI, bom suporte a métricas e boa DX. |

### Testes de Interface

| Tecnologia | Veredito | Justificativa |
| :--- | :--- | :--- |
| Selenium WebDriver | Rejeitado | API mais verbosa e complexa; manutenção mais difícil para suites modernas de UI. |
| Cypress | Rejeitado (para este contexto) | Excelente para front-end, mas já temos decisão prévia de focar API E2E no backend (ADR-018). Quando a UI existir, Playwright oferece melhor alinhamento com pipelines multi-browser. |
| Playwright | Escolhido | Suporte a múltiplos browsers, boa DX, fácil integração com CI e screenshots/traces para diagnóstico. |

## Consequências

### Positivas

- Passamos a ter critérios objetivos de performance para fluxos críticos da Mecânica API.
- Redução do risco de regressões sob carga à medida que o volume de uso aumenta.
- Quando houver UI, teremos uma base clara para automatizar as principais jornadas do usuário, reduzindo dependência de testes manuais.
- Os testes de carga ajudam a dimensionar infraestrutura e capacidade (número de pods, conexões de banco, etc.).

### Negativas

- Adoção de k6 e Playwright adiciona novas ferramentas ao stack, exigindo curva de aprendizado.
- Execução de testes de carga pode consumir recursos significativos de ambiente; precisa ser bem planejada para não afetar ambientes compartilhados.
- Testes de interface tendem a ser mais frágeis (flaky) e exigem disciplina em design de seletores de UI e isolamento de dados.

## Plano de Implementação

1. Criar o diretório `load-tests/` na raiz do repositório com scripts k6 para os fluxos definidos (autenticação, criação de OS, priorização, execução, relatórios).
2. Definir um comando padrão no `Makefile` (por exemplo, `make load-test`) para executar um conjunto básico de cenários de carga via k6 (local ou via Docker).
3. Documentar no `docs/qualidade_testes.md` como interpretar os resultados de carga (latência, throughput, erros) e qual o alvo mínimo aceitável.
4. Quando uma UI web para a Mecânica estiver disponível:
   - Criar um projeto dedicado de testes de interface com Playwright.
   - Configurar cenários para as principais jornadas do usuário (Atendente, Mecânico, Cliente, Admin).
   - Integrar a execução dos testes de interface na pipeline de CI, rodando contra um ambiente de teste com backend ativo.
5. Revisar periodicamente os cenários de carga e interface à medida que novas funcionalidades críticas forem adicionadas.

## Relação com Outras ADRs

- ADR-018: Estratégia de Testes End-to-End (E2E).
- ADR-027: Estratégia de Testes Integrados.
- ADR-028: Reestruturação da documentação de testes e qualidade.

