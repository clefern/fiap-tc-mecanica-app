# ADR-021: Flexibilização da Atribuição de Mecânicos

## Status
Aceita

## Contexto
Atualmente, a entidade `OrdemServico` possui apenas um campo `mecanicoId`, que representa o mecânico responsável pela OS. Este vínculo é criado no momento em que a OS transita para o status `EM_DIAGNOSTICO` e permanece imutável até o fim do ciclo de vida da OS, a menos que haja intervenção direta no banco de dados.

Esta rigidez apresenta problemas operacionais:
1.  **Especialização**: Frequentemente, um mecânico sênior realiza o diagnóstico, mas um mecânico júnior ou especialista executa o serviço.
2.  **Disponibilidade**: Se o mecânico responsável adoecer ou entrar de férias, a OS fica "travada".
3.  **Rastreabilidade**: Não distinguimos quem fez o diagnóstico (e gerou o orçamento) de quem executou o serviço. Se houver erro no diagnóstico, precisamos saber quem foi o responsável, mesmo que outro tenha executado.

## Decisão
Decidimos refatorar o modelo de atribuição de responsabilidades da Ordem de Serviço para suportar múltiplos atores e transferências de responsabilidade.

### 1. Separação de Responsabilidades
Introduziremos um novo conceito na entidade `OrdemServico`:
- **`mecanicoDiagnosticoId`**: Identificador do mecânico que realizou a fase de diagnóstico. Este campo é preenchido na transição para `EM_DIAGNOSTICO` e deve ser imutável via fluxo normal (exceto correções administrativas).
- **`mecanicoId` (Mantido)**: Representa o mecânico **atualmente responsável** pela OS. Durante o diagnóstico, é o mesmo do `mecanicoDiagnosticoId`. Durante a execução, pode ser alterado.

### 2. Persistência no Orçamento
A entidade `Orcamento` também deverá armazenar o `mecanicoDiagnosticoId` no momento de sua criação. Isso garante que, mesmo que a OS mude de mãos posteriormente, o documento de orçamento preserve a autoria técnica original.

### 3. Permissões de Troca
A troca do `mecanicoId` (responsável atual) será permitida através de um caso de uso específico (`TrocarMecanicoResponsavel`), restrito a usuários com perfil de **GERENTE** ou **ADMIN**. Mecânicos não podem "roubar" OSs de colegas arbitrariamente.

### 4. Fluxo de Atribuição
- **Iniciar Diagnóstico**: O sistema registra o usuário atual (ou o indicado) como `mecanicoId` E `mecanicoDiagnosticoId`.
- **Iniciar Execução**: O sistema verifica se o `mecanicoId` está preenchido. Se necessário, um Gerente pode atribuir um novo mecânico antes desta etapa.

## Consequências

### Positivas
- **Flexibilidade Operacional**: Permite que diferentes mecânicos atuem em fases diferentes.
- **Melhor Auditoria**: Clareza sobre quem diagnosticou vs. quem executou.
- **Continuidade**: OSs não ficam paradas por ausência de um funcionário específico.

### Negativas
- **Complexidade**: Adiciona mais campos e regras de transição.
- **Migração**: Requer atualização do esquema de banco de dados e ajuste de dados legados (onde `mecanicoDiagnosticoId` será igual a `mecanicoId` inicial).

## Implementação Técnica
1.  Criar migration Flyway adicionando coluna `mecanico_diagnostico_id` em `ordens_servico` e `orcamentos`.
2.  Atualizar entidades JPA e Domínio.
3.  Criar endpoint `PUT /api/os/{id}/atribuir-mecanico` (Admin/Gerente).
4.  Ajustar serviço de criação de orçamento para snapshotar o mecânico.
