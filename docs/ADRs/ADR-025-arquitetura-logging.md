# ADR-025 – Arquitetura de Logging na Aplicação Mecanica

## 1. Contexto

- **Problema atual**  
  - Classes de aplicação, em especial serviços ligados ao fluxo de Ordem de Serviço (ex.: `OrdemServicoServiceImpl`), concentram muitos logs operacionais, muitas vezes em nível de detalhe excessivo.  
  - Isso torna difícil diferenciar o que é evento de negócio relevante do que é ruído técnico, prejudicando a leitura em produção e a análise de incidentes.
- **Necessidade**  
  - Definir uma **arquitetura de logging em alto nível**, alinhada aos subdomínios e fluxos de negócio críticos (OS, orçamento, prioridade, estoque, etc.).  
  - Garantir que eventos importantes do domínio sejam registrados de forma consistente, rastreável e sem vazamento de dados sensíveis.  
  - Reduzir verbosidade excessiva em classes de serviço, evitando logs repetitivos de baixo valor.
- **Requisitos**  
  - Não expor dados sensíveis (tokens, senhas, e-mails completos, dados pessoais, etc.).  
  - Tratar com cuidado especial os passos críticos do core de negócio (mudanças de status de OS, violações de prioridade, falhas de integração com estoque, faturamento e autenticação).  
  - Melhorar a rastreabilidade, permitindo acompanhar o ciclo de vida de uma OS e eventos relacionados de forma clara.  
  - Reaproveitar decisões anteriores sobre padronização de exceções (ADR-008), em especial o uso de prefixo `❌` e a distinção entre exceções de negócio e de sistema.


## 2. Decisão Proposta

### 2.1 Localização arquitetural do logging

- **Camada recomendada para logging explícito**:  
  - Logging ficará **principalmente nas camadas de aplicação e infraestrutura**, que são responsáveis por orquestrar casos de uso, interagir com o mundo externo (HTTP, DB, integrações) e traduzir erros de domínio.  
  - A camada de **domínio** (entidades, VOs, serviços de domínio puros) deve permanecer, em geral, **sem logging**, focada em regras de negócio puras, determinísticas e facilmente testáveis.
- **Exceções pontuais**:  
  - Logs diretamente no domínio só serão aceitos em cenários muito específicos em que um serviço de domínio seja utilizado por múltiplas camadas e um evento crítico precise ser rastreado sem depender de infraestrutura. Esses casos devem ser raros e documentados.

### 2.2 Níveis hierárquicos de log

- **ERROR**  
  - Falhas de sistema e exceções inesperadas que impedem a conclusão de um caso de uso (ex.: falha de acesso ao banco, indisponibilidade de serviços externos).  
  - Deve seguir o padrão da ADR-008 para mensagens de exceção: prefixo `❌`, incluindo contexto suficiente para troubleshooting, sem expor dados sensíveis.
- **WARN**  
  - Situações anômalas, mas que não impedem a conclusão do fluxo (ex.: tentativa de violação de prioridade, dados inconsistentes vindos de sistemas externos, uso de fallback).  
  - Também podem usar prefixos específicos (`⚠️`) quando for relevante para destacar riscos de negócio.
- **INFO**  
  - Marcos importantes de negócio (ex.: OS criada, OS em diagnóstico, OS aprovada, OS cancelada, OS finalizada, orçamento emitido, violação de prioridade registrada).  
  - Usado para rastreio do fluxo de negócio em ambiente produtivo.
- **DEBUG**  
  - Detalhes técnicos úteis para investigação em ambientes de desenvolvimento ou homologação (payloads reduzidos, parâmetros de métodos, tempos de resposta).  
  - Em produção, o nível padrão será **INFO**, permitindo aumentar para DEBUG apenas em cenários específicos de troubleshooting controlado.

### 2.3 Padronização de formatos de mensagem

- **Mensagens de negócio (application/domain-facing)**  
  - Formato base:  
    `"[EVENTO_NEGOCIO] <contexto> | OS={} | Cliente={} | Mecanico={}"`  
  - Sempre que possível, incluir identificadores estáveis (UUID da OS, do cliente, do mecânico, código da OS) em vez de dados sensíveis.
- **Mensagens técnicas (infraestrutura, integrações)**  
  - Formato base:  
    `"[EVENTO_TECNICO] <descricao> | recurso={} | status={} | correlationId={}"`  
  - Direcionadas a chamadas de APIs externas, acesso a banco, caches, filas, etc.
- **Mensagens de erro/violação**  
  - Padronizar o uso de prefixos:  
    - `❌` para erros de sistema ou violações críticas.  
    - `⚠️` para avisos de negócio relevantes (ex.: tentativa de violação de prioridade).  
  - Manter coerência com ADR-008 (exceções específicas por contexto de negócio).


## 3. Detalhes de Implementação

### 3.1 Biblioteca de logging

- A aplicação utiliza **Spring Boot 3.x**, que por padrão vem com **SLF4J + Logback** via `spring-boot-starter-logging`.  
- Decisão:  
  - **Manter SLF4J como API de logging** e **Logback como implementação padrão**, evitando adicionar novas dependências de logging (como Log4j2) para reduzir complexidade operacional.  
  - Reforçar o uso de **Lombok `@Slf4j`** em componentes de aplicação/infraestrutura quando adequado, e `LoggerFactory.getLogger(...)` onde Lombok não estiver disponível.

### 3.2 Configuração centralizada

- Centralizar a configuração em `logback-spring.xml` (ou equivalente suportado pelo Spring Boot), com:  
  - **Appenders** separados para console e arquivo (e futuramente, integração com observabilidade).  
  - **Padrão de layout** incluindo timestamp, nível, logger, thread e, se disponível, `correlationId`/`traceId`.  
  - Níveis padrão por pacote:  
    - `INFO` para `com.fiap.mecanica.application` e `com.fiap.mecanica.presentation`.  
    - `WARN` ou superior para `com.fiap.mecanica.infra` onde houver muito ruído técnico.  
    - Possibilidade de elevar pontualmente para `DEBUG` via configuração de ambiente.

### 3.3 Convenções de uso

- **Mensagens de negócio (domínio específico)**  
  - Devem ser emitidas preferencialmente em **serviços de aplicação** (ex.: `OrdemServicoServiceImpl`, `OrcamentoServiceImpl`, `PrioridadeServiceImpl`) e em **listeners de eventos de domínio**.  
  - Exemplo:  
    `log.info("[OS_STATUS] OS={} mudou de {} para {}", os.getId(), statusAnterior, statusAtual);`
- **Mensagens técnicas**  
  - Localizadas em adaptadores de infraestrutura (gateways de estoque, integrações com e-mail, PDF, persistência) e filtros de segurança.  
  - Devem registrar URLs, códigos de status, tempos de resposta e identificadores de correlação, evitando payloads completos em produção.
- **Tratamento de dados sensíveis**  
  - É proibido logar: senhas, tokens, dados de cartão, e dados pessoais completos (CPF completo, e-mail completo, telefone completo).  
  - Quando necessário, mascarar:  
    - CPF: `***.***.***-**`  
    - E-mail: apenas domínio ou formato parcial (ex.: `f***@dominio.com`).  
    - Telefone: apenas DDD + final parcial.
- **Alinhamento com exceções (ADR-008)**  
  - Toda exceção específica deve seguir a hierarquia `MecanicaException -> BusinessException/SystemException`.  
  - Logs devem ser feitos na borda (application/infra), usando `logger.error` para erros de sistema e `logger.warn`/`logger.info` para exceções de negócio esperadas, sempre com prefixo `❌` quando for erro crítico.


## 4. Melhorias Propostas

### 4.1 Pontos críticos com necessidade de logging

- **Fluxo de Ordem de Serviço**  
  - Criação, entrada em diagnóstico, emissão de orçamento, aprovação, cancelamento, finalização e entrega.  
  - Toda mudança de status deve gerar um log `INFO` com identificadores da OS e contexto relevante.
- **Prioridade de OS**  
  - Violações de prioridade (já parcialmente cobertas em `PrioridadeServiceImpl`) devem continuar registradas em `ERROR` com prefixo `❌`, mantendo o padrão existente.
- **Estoques e insumos**  
  - Falhas ao reservar peças/insumos e inconsistências de estoque devem gerar logs `WARN` ou `ERROR` com contexto suficiente para diagnóstico.

### 4.2 Análise e refino de código existente

- **Remoção de logs redundantes**  
  - Revisar serviços muito verbosos (como `OrdemServicoServiceImpl`) para:  
    - Eliminar logs que apenas repetem dados já disponíveis em outros pontos (ex.: logs duplicados em cada chamada de repositório).  
    - Substituir múltiplos logs de baixo valor por um único log bem estruturado por evento de negócio.
- **Consolidação de mensagens**  
  - Agrupar informações relevantes em mensagens únicas (por exemplo, um log de mudança de status da OS que também informe se houve disparo de evento de domínio associado).  
  - Evitar logs dentro de loops intensivos em coleções, preferindo um resumo final.
- **Granularidade adequada**  
  - Usar `DEBUG` para detalhes que só são necessários em desenvolvimento (ex.: conteúdo de requisições externas, parâmetros completos), mantendo produção em `INFO` por padrão.

### 4.3 Diretrizes para novos desenvolvimentos

- Todo novo caso de uso deve responder às perguntas:  
  - Quais são os **eventos de negócio** importantes que merecem log `INFO`?  
  - Quais são os **erros de sistema** que precisam de log `ERROR` com contexto?  
  - Há alguma situação de **risco de negócio** que merece `WARN`?  
- Novas funcionalidades devem seguir o padrão de prefixos (`❌`, `⚠️`) e formatação descritos neste ADR.


## 5. Impactos

- **Performance**  
  - Logs em excesso podem afetar I/O e aumentar o tempo de resposta. A consolidação e redução de verbosidade em serviços críticos tende a **melhorar a performance percebida**.  
  - O uso cuidadoso de `DEBUG` evita overhead desnecessário em produção.
- **Armazenamento**  
  - Menos logs redundantes significam menor volume em disco e em ferramentas de observabilidade, reduzindo custo de armazenamento e retenção.  
  - A padronização de formatos facilita compressão e indexação eficiente.
- **Facilidade de troubleshooting**  
  - Com mensagens padronizadas por evento de negócio e por tipo de erro, a busca em logs (por OS, cliente, evento, etc.) fica muito mais simples.  
  - O alinhamento com exceções específicas (ADR-008) e com eventos de domínio melhora a rastreabilidade ponta a ponta.


## 6. Critérios de Aceitação

- Redução da verbosidade de logs em serviços problemáticos (como o fluxo de OS) em **X%** (meta inicial sugerida: 30–50%), medida pela contagem média de linhas de log por requisição em ambiente de teste/homologação.  
- Cobertura de logging em **100% dos pontos críticos de negócio** identificados (mudanças de status de OS, emissão/aprovação/cancelamento de orçamento, violações de prioridade, falhas de integração com estoque e faturamento).  
- Documentação clara das convenções de logging em nível de projeto, referenciando este ADR em guias de contribuição e revisão de código.  
- Testes automatizados que validem formatação e filtros de log em pontos críticos (por exemplo, testes de integração que verificam que mensagens esperadas são emitidas ao executar determinados fluxos, sem expor dados sensíveis).


## 7. Próximos Passos

- **Plano de migração gradual**  
  - Etapa 1: Revisar e refatorar logs no fluxo de Ordem de Serviço (criação, diagnóstico, orçamento, aprovação, cancelamento, finalização, entrega).  
  - Etapa 2: Aplicar o padrão aos serviços de orçamento, prioridade e estoque.  
  - Etapa 3: Ajustar logs de infraestrutura (e-mails, PDF, integrações externas) para seguir a mesma convenção.
- **Treinamento do time**  
  - Realizar uma sessão de alinhamento com o time sobre este ADR, mostrando exemplos práticos de mensagens de log boas e ruins, e como aplicar os padrões em PRs.
- **Monitoramento contínuo da eficácia**  
  - Acompanhar incidentes e sessões de troubleshooting para avaliar se os logs são suficientes e úteis.  
  - Revisitar este ADR caso sejam identificadas lacunas (ex.: necessidade de novos prefixos, campos adicionais em mensagens, ou ajustes de nível).

