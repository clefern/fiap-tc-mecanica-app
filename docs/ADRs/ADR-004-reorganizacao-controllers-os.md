# ADR-004: Reorganização dos Controllers de Ordem de Serviço

## Status
Aceito

## Data
2025-01-15

## Contexto
Recentemente, implementamos uma **Task-Based API** para gerenciar as transições de status da Ordem de Serviço (OS), introduzindo o `OrdemServicoAcoesController`. Isso criou uma dualidade com o `OrdemServicoController` existente, que possui um endpoint genérico `PATCH /status` e endpoints para gestão de itens.

Precisamos definir claramente as responsabilidades de cada controller para evitar débito técnico, confusão no consumo da API e garantir que as regras de negócio (agora ricas no domínio) sejam respeitadas em todos os fluxos, inclusive no seeding de dados.

## Análise da Estrutura Atual

1.  **OrdemServicoController (Legacy/CRUD):**
    *   Gerencia o ciclo de vida básico (Create, Read, List).
    *   Contém `PATCH /status` (anêmico e perigoso, pois permite saltos de status inválidos se não validado estritamente).
    *   Gerencia sub-recursos (`POST /itens`, `DELETE /itens`).
2.  **OrdemServicoAcoesController (New/Task-Based):**
    *   Gerencia transições de estado explícitas (`iniciar-diagnostico`, `aprovar`, etc.).
    *   Aplica regras de negócio estritas e RBAC (Role-Based Access Control).
3.  **Seeding/Factory:**
    *   Atualmente cria OSs e define status artificialmente. Com as novas regras de domínio (ex: não pode aprovar sem itens), o seeding antigo pode gerar estados inconsistentes se apenas "setar" o status final.

## Decisão

Decidimos reorganizar a arquitetura de controllers e seeding da seguinte forma:

### 1. Separação de Responsabilidades (Controllers)

*   **OrdemServicoController (Resource Controller)**
    *   **Manter:** Criação (`POST /`), Listagem (`GET /`), Busca por ID (`GET /{id}`).
    *   **Manter:** Gestão de Itens (`POST /{id}/itens`, `DELETE /{id}/itens/{itemId}`).
        *   *Justificativa:* Itens são sub-recursos da OS. Adicioná-los é uma operação de composição de recurso, não necessariamente uma transição de fluxo de trabalho.
    *   **Deprecar:** `PATCH /{id}/status`.
        *   *Ação:* Manter o método anotado com `@Deprecated` para retrocompatibilidade imediata, mas remover a lógica de negócio interna e redirecionar (se possível) ou apenas manter para clientes legados até a próxima versão major.
        *   *Obs:* Como este é um projeto novo/acadêmico, podemos optar por **remover** completamente para garantir integridade. **Decisão final: Remover `PATCH /status`** para forçar o uso da API correta.

*   **OrdemServicoAcoesController (Workflow Controller)**
    *   **Manter:** Todos os endpoints de ação (`iniciar-diagnostico`, `emitir-orcamento`, `aprovar`, `finalizar`, etc.).
    *   **Expandir:** Futuras ações de negócio (ex: `solicitar-peca`) devem residir aqui.

### 2. Refatoração do Seeding (Factory & Service)

O `OrdemServicoFactory` não deve mais "fabricar" uma OS em estado final (ex: FINALIZADA) apenas setando o atributo. Isso viola as invariantes do domínio rico.

*   **OrdemServicoFactory:** Deve ser responsável apenas por criar a OS no estado inicial válido (`RECEBIDA`).
*   **SeedingService:** Deve atuar como um "simulador de uso". Para criar uma OS `FINALIZADA`, ele deve:
    1.  Criar OS (Factory -> `RECEBIDA`).
    2.  Adicionar Itens (Service -> `adicionarItem`).
    3.  Chamar Service -> `iniciarDiagnostico`.
    4.  Chamar Service -> `emitirOrcamento`.
    5.  Chamar Service -> `aprovar`.
    6.  Chamar Service -> `finalizar`.

Isso garante que o banco de dados de desenvolvimento/teste sempre contenha dados que são *possíveis* de serem gerados pela aplicação real.

## Consequências

### Positivas
*   **Consistência:** Impossível criar dados inválidos via API ou Seeding.
*   **Clareza:** A API comunica explicitamente o que pode ser feito.
*   **Manutenibilidade:** Testes e Seeding validam o fluxo real de negócio, servindo como testes de fumaça (smoke tests).

### Negativas
*   **Complexidade no Seeding:** O código de seeding ficará mais verboso e lento, pois executará lógicas de validação para cada transição.
*   **Refatoração:** Necessário ajustar testes unitários que dependiam do `setStatus` para setup rápido (ainda permitido em testes unitários via mocks ou builders de teste, mas desencorajado em testes de integração).

## Plano de Implementação

1.  **OrdemServicoController:**
    *   Remover método `atualizarStatus`.
    *   Garantir que `adicionarItem` e `removerItem` estejam funcionando e validados (ex: não pode adicionar item em OS fechada).
2.  **OrdemServicoFactory:**
    *   Simplificar para retornar apenas OSs novas.
3.  **SeedingService:**
    *   Implementar pipeline de transição de status.
    *   Exemplo: `seedFinalizada()` chama a cadeia completa de métodos do Service.
4.  **Testes:**
    *   Corrigir testes que quebrarem com a remoção do `PATCH /status`.

## Cronograma Sugerido
*   **Imediato:** Remoção do endpoint legado e ajuste do Controller.
*   **Próxima Sessão:** Refatoração do SeedingService para usar o fluxo transacional.

