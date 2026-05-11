# ADR-005: Gestão de Serviços Adicionais em OS Aguardando Aprovação

**Data:** 2025-01-15
**Autores:** Time Mecânica
**Status:** Aceito

## 1. Contexto

### Descrição do Cenário Atual
O sistema de gerenciamento de oficina mecânica utiliza um fluxo de estados para a Ordem de Serviço (OS). Atualmente, quando uma OS atinge o estado `AGUARDANDO_APROVACAO`, um orçamento já foi emitido e o cliente deve aprovar ou reprovar o serviço.
A regra de negócio atual (`OrdemServico.java`) bloqueia a adição ou remoção de itens quando a OS não está nos estados `RECEBIDA` ou `EM_DIAGNOSTICO`.

### Requisito Novo
Frequentemente, clientes solicitam serviços adicionais após receberem o orçamento inicial, mas antes de aprová-lo (enquanto a OS está em `AGUARDANDO_APROVACAO`).
Exemplo: O cliente recebe o orçamento para troca de óleo, mas decide pedir também a troca das pastilhas de freio antes de aprovar.

### Impactos no Fluxo de Trabalho
O fluxo atual exigiria que o mecânico ou atendente tivesse uma forma de "reabrir" o orçamento para incluir os novos itens, garantindo que o valor total seja recalculado e uma nova aprovação seja solicitada sobre o todo.

## 2. Opções de Implementação Avaliadas

### Opção A: Criar uma Nova OS Separada
Criar uma segunda Ordem de Serviço vinculada ao mesmo veículo para os serviços adicionais.
- **Fluxo:** OS 1 (Óleo) fica aguardando aprovação. OS 2 (Freio) é criada, passa por diagnóstico e gera um segundo orçamento.

### Opção B: Atualizar a OS Existente (Revertendo Estado)
Permitir que a OS retorne ao estágio de diagnóstico para adição de novos itens, invalidando o orçamento anterior.
- **Fluxo:** OS volta de `AGUARDANDO_APROVACAO` para `EM_DIAGNOSTICO`. Itens são adicionados. Novo orçamento é emitido. OS volta para `AGUARDANDO_APROVACAO`.

### Opção C: Sistema de Modificação com Versionamento
Implementar um sistema complexo onde o orçamento possui versões (v1, v2) dentro da mesma OS, permitindo histórico de alterações sem necessariamente voltar o status da OS.

## 3. Critérios de Avaliação

1.  **Tempo de processamento total:** Rapidez para o atendente realizar a operação.
2.  **Complexidade de implementação:** Esforço de desenvolvimento e risco de bugs.
3.  **Impacto na experiência do cliente:** Facilidade para o cliente entender e aprovar.
4.  **Conformidade com políticas:** Garantia de que nada será executado sem aprovação do valor total.
5.  **Rastreabilidade:** Capacidade de saber o que mudou.

## 4. Análise Detalhada

### Opção A: Nova OS Separada
- **Vantagens:**
    - Isolamento total dos serviços.
    - Não exige alteração na máquina de estados atual.
- **Desvantagens:**
    - Péssima experiência para o cliente (duas aprovações, duas cobranças, dois acompanhamentos).
    - Poluição do banco de dados com múltiplas OSs para a mesma visita.
    - Dificuldade logística (um mecânico pode pegar a OS 1 e outro a OS 2 para o mesmo carro).

### Opção B: Atualizar OS Existente (Recomendada)
- **Vantagens:**
    - Experiência unificada para o cliente (um único orçamento consolidado).
    - Simplicidade operacional (tudo numa única ficha).
    - Aproveita a transição de estado já existente no domínio (`AGUARDANDO_APROVACAO` -> `EM_DIAGNOSTICO`).
- **Desvantagens:**
    - Perde-se o registro do "primeiro orçamento" se não houver log de auditoria (mitigável).
- **Requisitos Técnicos:**
    - Expor endpoint para transição de retorno (`reiniciarDiagnostico`).
    - Garantir que `emitirOrcamento` recalcule tudo corretamente.

### Opção C: Versionamento de Orçamento
- **Vantagens:**
    - Histórico detalhado de negociação.
- **Desvantagens:**
    - Alta complexidade de implementação (tabelas de histórico, controle de versão de agregados).
    - Overengineering para o estágio atual do projeto.
    - Aumenta a carga cognitiva do usuário (qual versão estou aprovando?).

## 5. Recomendação

**Decisão:** Adotar a **Opção B (Atualizar a OS Existente)**.

**Justificativa:**
Esta abordagem oferece o melhor equilíbrio entre simplicidade técnica e experiência do usuário. O modelo de domínio atual (`OrdemServico.java`) já prevê a transição de `AGUARDANDO_APROVACAO` para `EM_DIAGNOSTICO` (linhas 138-139), o que torna a implementação natural e segura. Ao voltar para diagnóstico, o sistema implicitamente diz "o orçamento anterior não vale mais, estamos avaliando novamente".

**Plano de Implementação:**
1.  Verificar se o endpoint `iniciarDiagnostico` ou similar permite a chamada quando o status é `AGUARDANDO_APROVACAO`.
2.  Se não houver endpoint explícito para "retornar", criar uma ação `revisarOrcamento` no controller `OrdemServicoAcoesControllerApi` que chama o método de domínio para transitar o status.
3.  O frontend/cliente deve ser notificado que o orçamento anterior foi cancelado/substituído.

**Plano de Rollback:**
Caso a mudança cause inconsistências, a reversão é puramente de código (remover a permissão de transição), voltando ao comportamento anterior (bloqueio).

## 6. Considerações Futuras

- **Histórico de Alterações:** Futuramente, podemos implementar uma tabela de auditoria (`OrdemServicoAudit`) para guardar snapshots dos orçamentos gerados, para fins estatísticos (ex: "quanto o cliente cortou do orçamento original?").
- **Aprovação Parcial:** Avaliar a necessidade de permitir que o cliente aprove apenas *alguns* itens do orçamento (atualmente o modelo é tudo ou nada).
