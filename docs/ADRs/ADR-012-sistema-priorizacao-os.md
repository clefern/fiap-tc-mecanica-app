# ADR-012: Sistema de Priorização de Ordens de Serviço (OS)

**Status**: Proposto
**Data**: 2026-01-11
**Contexto**: Mecânica System - Módulo de Gestão de OS

## 1. Contexto e Problema

Atualmente, o sistema de gestão de oficinas não possui um mecanismo formal para determinar a ordem de atendimento das Ordens de Serviço. Isso gera ineficiências operacionais, como:
- Veículos parados há muito tempo sendo ignorados em favor de novos.
- Casos urgentes (ex: garantias ou veículos de emergência) sem distinção visual ou sistêmica.
- Falta de transparência para o cliente sobre a posição do seu serviço na fila.
- Dependência da memória ou decisão subjetiva do gerente/mecânico para escolher a próxima tarefa.

Necessita-se de um sistema determinístico e auditável que ordene as filas de **Análise/Orçamento** e **Execução**.

## 2. Critérios de Priorização Propostos

O sistema adotará uma abordagem híbrida: **Classificação de Urgência + FIFO (First-In, First-Out)**.

### 2.1. Níveis de Prioridade (Classificação Geral)
Será introduzido um campo de prioridade explícito (Enum/Nível) que serve como o **primeiro critério de ordenação**.

| Nível | Descrição | Cenário de Uso |
|-------|-----------|----------------|
| **URGENTE (3)** | Atendimento imediato | Garantias, Retornos, Veículos de Emergência. |
| **ALTA (2)** | Prioridade sobre o fluxo normal | Clientes VIP, Prazos críticos acordados. |
| **NORMAL (1)** | Fluxo padrão (Default) | Manutenções de rotina, revisões. |
| **BAIXA (0)** | Pode aguardar | Serviços internos, cortesia sem prazo. |

### 2.2. Regras de Ordenação por Fila

#### A. Fila de Análise / Orçamento
Destina-se a mecânicos que farão o diagnóstico inicial.
- **Filtro**: Status = `RECEBIDA`
- **Ordenação Primária**: Nível de Prioridade (Decrescente: Urgente -> Baixa)
- **Ordenação Secundária**: Data de Criação da OS (Crescente: Mais antigas primeiro)

#### B. Fila de Execução
Destina-se a mecânicos que executarão o serviço aprovado.
- **Filtro**: Status = `APROVADA`
- **Ordenação Primária**: Nível de Prioridade (Decrescente)
- **Ordenação Secundária**: Data de Aprovação do Orçamento (Crescente: Mais antigas primeiro)

## 3. Implementação Técnica

### 3.1. Alterações no Modelo de Dados

#### Tabela `ordens_servico`
Adição de campos para suporte à priorização e ordenação.

- `prioridade` (ENUM/INTEGER): Armazena o nível (0=BAIXA, 1=NORMAL, 2=ALTA, 3=URGENTE). Default: `NORMAL`.
- `data_aprovacao` (TIMESTAMP): Já existente ou necessário criar para persistir o momento exato da aprovação (mudança de status para APROVADA).

#### Tabela `itens_ordem_servico` (Opcional/Futuro)
- `prioridade_execucao` (INTEGER): Para casos onde uma OS tem múltiplos serviços e um deve ser feito antes do outro (ex: desmontagem antes da retífica). *Inicialmente fora do escopo do MVP, mantendo a prioridade na OS inteira.*

### 3.2. Fluxo de Trabalho e API

1.  **Endpoint de Listagem de Orçamento (Queue)**
    - `GET /api/os/fila-orcamento`
    - Query interna: `SELECT * FROM os WHERE status = 'RECEBIDA' ORDER BY prioridade DESC, created_at ASC`

2.  **Endpoint de Listagem de Execução (Queue)**
    - `GET /api/os/fila-execucao`
    - Query interna: `SELECT * FROM os WHERE status = 'APROVADA' ORDER BY prioridade DESC, data_aprovacao ASC`

3.  **Ajuste Manual (Gerente)**
    - `PATCH /api/os/{id}/prioridade`
    - Body: `{ "prioridade": "ALTA", "motivo": "Cliente reclamou da demora" }`
    - Ação: Atualiza o campo `prioridade` e registra no histórico.

### 3.3. Performance (Índices)
Para garantir resposta rápida nas filas:
- Index 1: `CREATE INDEX idx_os_fila_orcamento ON ordens_servico (status, prioridade DESC, created_at ASC) WHERE status = 'RECEBIDA';`
- Index 2: `CREATE INDEX idx_os_fila_execucao ON ordens_servico (status, prioridade DESC, data_aprovacao ASC) WHERE status = 'APROVADA';`

## 4. Regras de Negócio e Validação

1.  **Imutabilidade Automática**: O sistema define a prioridade `NORMAL` na criação. Apenas usuários com role `GERENTE` ou `ADMIN` podem alterar manualmente para `ALTA` ou `URGENTE`.
2.  **Registro de Auditoria**: Toda alteração manual de prioridade deve gerar um log ou evento de domínio (`PrioridadeAlteradaEvent`) contendo:
    - Usuário responsável.
    - Prioridade anterior e nova.
    - Motivo da alteração.
3.  **Consistência de Datas**: A `data_aprovacao` não pode ser nula se o status for `APROVADA`. Deve ser preenchida no momento da transição de status.

## 5. Considerações de UI/UX

### Painel de Controle (Kanban ou Lista)
- **Indicadores Visuais**:
    - Ícone/Borda Vermelha para `URGENTE`.
    - 🟡 Ícone/Borda Amarela para `ALTA`.
    - Neutro para `NORMAL`/`BAIXA`.
- **Informações do Card**:
    - Posição na fila (ex: #1, #2).
    - Tempo de espera (ex: "Aguardando há 2 dias").
    - Placa do Veículo e Modelo.

### Ajuste Manual
- Modal acessível apenas para perfis autorizados.
- Dropdown simples para seleção de nova prioridade.
- Campo de texto obrigatório para "Justificativa" se elevar a prioridade.

## 6. Próximos Passos e Prazos

| Fase | Atividade | Prazo Estimado |
|------|-----------|----------------|
| **Fase 1** | Migration (DB) e Atualização das Entidades JPA | 2 dias |
| **Fase 2** | Implementação dos Endpoints de Fila e Ajuste de Prioridade | 3 dias |
| **Fase 3** | Integração no Frontend (Visualização das Filas) | 4 dias |
| **Fase 4** | Testes de Carga e Validação de Regras | 2 dias |

### Testes de Validação
- **Unitários**: Garantir que a ordenação das listas (Collections) respeita a lógica `Prioridade > Data`.
- **Integração**: Verificar se as queries do repositório retornam os registros na ordem correta usando banco em memória H2/Testcontainers.
- **E2E**: Simular um fluxo onde uma OS `NORMAL` antiga é ultrapassada por uma `URGENTE` nova na fila.

### Documentação
- Atualizar o diagrama de estados da OS para incluir o gatilho de preenchimento da `data_aprovacao`.
- Treinamento rápido para os gerentes sobre o critério de uso da prioridade `URGENTE`.

## 7. Cenários de Alteração de Prioridade (Perspectiva de Domínio)

Embora a prioridade inicial seja definida como `NORMAL` e a ordenação natural ocorra pelas datas, existem cenários específicos onde a intervenção (manual ou automática) na prioridade é necessária para refletir a realidade do negócio:

1.  **Segurança Crítica (Escalação Técnica)**
    *   **Cenário**: Durante o diagnóstico, o mecânico identifica uma falha com risco iminente de acidente (ex: freios prestes a falhar).
    *   **Ação**: A OS deve ser elevada para `URGENTE` para garantir que o reparo seja realizado imediatamente, priorizando a segurança do cliente sobre a ordem de chegada.

2.  **Escalação de Negócio (VIP / Garantia / Retorno)**
    *   **Cenário**: Um cliente retorna com o mesmo problema (retorno de serviço) ou é um cliente corporativo com SLA contratual (ex: frota de ambulâncias).
    *   **Ação**: O gerente eleva a prioridade para `ALTA` ou `URGENTE` para cumprir contratos ou mitigar insatisfação.

3.  **Bloqueio Logístico (Downgrade Temporário)**
    *   **Cenário**: O serviço foi aprovado, mas as peças necessárias estão em falta e demorarão 3 dias para chegar.
    *   **Ação**: A OS pode ser temporariamente rebaixada para `BAIXA` ou movida para um status de "Bloqueio" (embora `BAIXA` mantenha a visibilidade na fila mas no final dela), permitindo que serviços com peças disponíveis passem na frente na fila de execução.

4.  **Aging (Envelhecimento da OS)**
    *   **Cenário**: Uma OS de prioridade `NORMAL` está na fila há mais de 5 dias sem ser iniciada.
    *   **Ação**: O sistema (via job noturno) pode elevar automaticamente para `ALTA` para evitar violação de SLA implícito e garantir que "os últimos não sejam esquecidos para sempre" se a demanda urgente for constante.
