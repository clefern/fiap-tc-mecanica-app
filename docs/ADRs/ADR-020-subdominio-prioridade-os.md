# ADR-020: Subdomínio de Prioridade e Validação Rigorosa de Filas

**Status**: Aceito
**Data**: 2026-01-14  
**Referência**: [A_next_technical_debts.md](../A_next_technical_debts.md)
**Relacionado a**: [ADR-012-sistema-priorizacao-os.md](ADR-012-sistema-priorizacao-os.md)

## 1. Contexto e Problema

Embora a ADR-012 tenha estabelecido os critérios e níveis de prioridade, a implementação atual ainda permite que ordens de serviço (OS) sejam processadas fora da ordem estabelecida. Não há um mecanismo rígido que impeça um mecânico de "escolher" uma OS mais simples em detrimento de uma prioritária (cherry-picking).

Além disso, as regras de negócio de prioridade estão dispersas, dificultando a manutenção e a garantia de que a fila está sendo respeitada tanto para orçamento quanto para execução.

É necessário criar um subdomínio dedicado para centralizar essas regras e impor validações estritas nos pontos de entrada do processo.

## 2. Decisão

Decidimos criar um **Subdomínio de Prioridade** dedicado e impor validações bloqueantes no fluxo de trabalho.

### 2.1. Estrutura do Subdomínio

Será criado um controlador específico `PrioridadeOsController` (e serviços associados) responsável exclusivamente pela gestão das filas.

**Endpoints Obrigatórios:**

1.  **`GET /api/prioridade/fila-orcamento`**:
    - Retorna a lista de OSs com status `RECEBIDA`, ordenadas por prioridade (descendente) e data (ascendente).
2.  **`GET /api/prioridade/fila-execucao`**:
    - Retorna a lista de OSs com status `APROVADA`, ordenadas por prioridade (descendente) e data de aprovação (ascendente).
3.  **`PUT /api/prioridade/{id}`**:
    - Permite atualizar a prioridade de uma OS (restringido a gerentes).
4.  **`GET /api/prioridade/proxima`**:
    - Endpoint unificado que retorna a **única** próxima OS prioritária a ser trabalhada pelo usuário solicitante (ou globalmente), deixando claro qual é a tarefa imediata.

### 2.2. Validação Rigorosa de Processamento

Para garantir a aderência à fila, os serviços de mudança de status (`iniciarDiagnostico` e `iniciarExecucao`) devem implementar a seguinte lógica de validação:

#### Para `iniciarDiagnostico`:
1.  O serviço deve consultar internamente a regra da `fila-orcamento`.
2.  **Validação Bloqueante**: Verificar se a OS solicitada para início é **estritamente a primeira** da lista (maior prioridade).
3.  **Ação em caso de violação**:
    - Bloquear a operação (lançar exceção de negócio).
    - Registrar um **Log de Erro Detalhado**.

#### Para `iniciarExecucao`:
1.  O serviço deve consultar internamente a regra da `fila-execucao`.
2.  **Validação Bloqueante**: Verificar se a OS solicitada é **estritamente a primeira** da lista.
3.  **Ação em caso de violação**:
    - Bloquear a operação.
    - Registrar um **Log de Erro Detalhado**.

### 2.3. Observabilidade e Logs de Erro

Qualquer tentativa de burlar a fila (iniciar uma OS que não é a primeira) deve gerar um log estruturado contendo:

- **Timestamp**: Data e hora exata da tentativa.
- **OS Incorreta**: ID da OS que o usuário tentou iniciar.
- **Contexto da Fila**: Lista das prioridades reais no momento (ex: "Tentou iniciar OS-10 (Normal) mas a OS-05 (Urgente) estava pendente").
- **Prioridade Real vs Esperada**.

Esses logs devem ser formatados para fácil ingestão por ferramentas de monitoramento centralizado.

## 3. Consequências

### Positivas
- **Eliminação do "Cherry-picking"**: Mecânicos são forçados a seguir a prioridade definida pelo negócio.
- **Centralização**: Toda lógica de ordenação fica em um único local, facilitando ajustes nos critérios de prioridade.
- **Transparência**: Fica claro para todos qual é a próxima tarefa a ser executada.
- **Auditoria**: Tentativas de desvio de processo são registradas e podem ser auditadas pela gerência.

### Negativas
- **Rigidez Operacional**: Em cenários onde a OS prioritária não pode ser iniciada por motivos externos (ex: falta de peça, impedimento físico), o sistema pode travar a fila.
    - *Mitigação*: Será necessário um mecanismo para "pular" ou "suspender" uma OS prioritária com justificativa, movendo-a temporariamente para fora do topo da fila (fora do escopo desta ADR, mas um ponto de atenção).
- **Latência Adicional**: A verificação da fila adiciona uma query extra antes de cada início de trabalho.

## 4. Plano de Testes

A implementação deve incluir testes automatizados cobrindo:
1.  **Sucesso**: Iniciar a primeira OS da fila funciona corretamente.
2.  **Bloqueio**: Tentar iniciar a segunda OS da fila gera erro e log.
3.  **Fila Vazia**: Comportamento adequado quando não há OSs na fila.
4.  **Concorrência**: Garantir que duas pessoas não iniciem a mesma OS simultaneamente (tratado pelo lock otimista/pessimista da entidade, mas reforçado aqui).
