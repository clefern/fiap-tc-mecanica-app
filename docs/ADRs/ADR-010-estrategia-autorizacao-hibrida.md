# ADR-010: Estratégia Híbrida de Autorização (RBAC + ABAC)

## Status
Proposto

## Data
2026-01-11

## 1. Contexto e Problema Atual

Atualmente, o sistema utiliza um modelo de autorização puramente baseado em Roles (RBAC - Role-Based Access Control) através das anotações `@PreAuthorize("hasRole('...')")` do Spring Security. Embora funcional para segregação básica de acesso, este modelo tem se mostrado insuficiente para as regras de negócio complexas do domínio de oficina mecânica.

### Problemas Identificados:

1.  **Granularidade Insuficiente**: Um usuário com role `ADMIN` (ex: `admin@mecanica`) tecnicamente possui permissão para acessar endpoints de mudança de status, mas não deveria, sob a ótica do negócio, intervir em processos estritamente operacionais (como reiniciar um diagnóstico já finalizado) sem uma justificativa ou fluxo de "break-glass".
2.  **Falta de Contexto**: A permissão para "Iniciar Execução" é concedida a qualquer `MECANICO`. Não há validação se o mecânico solicitante é o mesmo atribuído à Ordem de Serviço (OS).
3.  **Rastreabilidade Operacional**: Identificamos a necessidade crítica de rastrear o `mecanicoId` nas OSs, não apenas para auditoria, mas para cálculo de comissões e métricas de performance. O modelo atual de segurança não enforce a atribuição de responsabilidade.
4.  **Inconsistência de Estado**: Permissões de endpoint não consideram o estado da entidade. Ex: Um endpoint de `PATCH` pode estar aberto para um ADMIN mesmo que a OS esteja `FINALIZADA` e imutável.

## 2. Análise de Roles e Permissões

### Mapeamento Atual vs. Necessidade Real

| Role | Permissões Atuais (RBAC) | Discrepâncias e Necessidades (Least Privilege) |
| :--- | :--- | :--- |
| **ADMIN** | Acesso total irrestrito. | **Excesso de permissão**. Não deve executar tarefas operacionais (ex: trocar status para "Em Execução") exceto em correções de dados. Deve focar em gestão de usuários e configurações. |
| **ATENDENTE** | Gerencia OS (Cria, Entrega, Cancela). | Adequado, mas precisa ser impedido de alterar dados técnicos (diagnóstico) inseridos pelo mecânico. |
| **MECANICO** | Diagnóstico, Execução, Orçamento. | **Falta de restrição**. Pode alterar OSs de outros mecânicos. Precisa ser restrito às suas atribuições ou OSs livres. |
| **CLIENTE** | Aprova orçamento, Visualiza OS. | Precisa garantir isolamento estrito de dados (apenas suas OSs). |

## 3. Estratégia de Autorização Proposta

Adotaremos um modelo **Híbrido**, combinando RBAC (para proteção grossa de endpoints) com ABAC (Attribute-Based Access Control) e Context-Based Security (para regras de negócio finas).

### 3.1. Camadas de Segurança

1.  **Nível de Endpoint (RBAC)**: Verifica "Quem é você?" (Role).
    *   *Ex:* Apenas `MECANICO` pode acessar `POST /api/os/{id}/execucao`.
2.  **Nível de Recurso (Context/ABAC)**: Verifica "Você pode fazer ISSO com ESTE recurso NESTE estado?".
    *   *Ex:* O `MECANICO` (Role) pode iniciar a OS `{id}` SOMENTE SE `os.mecanicoId == user.id` (Atributo) E `os.status == APROVADA` (Estado).

### 3.2. Regras de Contexto Críticas

*   **Propriedade da Tarefa**: Operações técnicas (`iniciar-diagnostico`, `emitir-orcamento`, `iniciar-execucao`, `finalizar`) exigem que o usuário logado seja o `mecanicoResponsavel` da OS.
    *   *Exceção*: Se `mecanicoResponsavel` for nulo, a primeira ação atribui a OS ao usuário logado (Self-Assignment).
*   **Imutabilidade por Status**:
    *   Status `FINALIZADA`, `ENTREGUE`, `CANCELADA`: Bloqueio total de escrita para todas as roles.
    *   Status `AGUARDANDO_APROVACAO`: Bloqueio de edição de itens para Mecânicos.
*   **Isolamento do Cliente**: Clientes nunca podem acessar dados de outros clientes. O filtro deve ser aplicado preferencialmente na query (JPA) e reforçado na segurança (AccessDenied).

## 4. Implementação Técnica

### 4.1. Custom Security Expressions

Criaremos um componente `OsSecurity` (Bean Spring) para encapsular regras complexas, permitindo o uso de SpEL (Spring Expression Language) limpo nos Controllers.

```java
@Component("osSecurity")
public class OsSecurity {
    public boolean canManage(Authentication auth, UUID osId) {
        // 1. Carrega OS
        // 2. Verifica se User é dono ou se OS está livre
        // 3. Verifica status
    }
    
    public boolean isOwner(Authentication auth, UUID osId) { ... }
}
```

### 4.2. Aplicação nos Controllers

```java
@PostMapping("/{id}/iniciar-execucao")
@PreAuthorize("hasRole('MECANICO') and @osSecurity.canWorkOn(authentication, #id)")
public ResponseEntity<Void> iniciarExecucao(@PathVariable UUID id) { ... }
```

### 4.3. Middleware de Atribuição Automática

No serviço, ao realizar uma ação de transição de status (ex: `iniciar-diagnostico`), se a OS não tiver mecânico, o sistema deve atribuir automaticamente o usuário logado do contexto de segurança.

### 4.4. Tratamento de Erros

*   **401 Unauthorized**: Falta de token ou token inválido.
*   **403 Forbidden**: Token válido, mas regra de negócio de segurança falhou (ex: Mecânico tentando mexer na OS de outro).
    *   *Payload*: Deve diferenciar erro de Role vs. erro de Regra de Negócio (ex: "Esta OS pertence a outro mecânico").

## 5. Documentação

O `README.md` e a documentação da API devem incluir:

1.  **Matriz de Autorização**: Tabela cruzando `Role` x `Ação` x `Status Permitido`.
2.  **Glossário de Erros de Segurança**: Explicação dos códigos 403 específicos.
3.  **Fluxo de Atribuição**: Como um mecânico "pega" uma OS para si.

## 6. Plano de Transição

| Fase | Ação | Estimativa |
| :--- | :--- | :--- |
| **1. Fundação** | Criar `OsSecurity` Bean e implementar testes unitários das regras. | 1 Sprint |
| **2. Atribuição** | Alterar `OrdemServicoService` para popular `mecanicoId` baseado no usuário logado. | 1 Sprint |
| **3. Migração** | Atualizar `OrdemServicoAcoesController` com novas anotações SpEL. | 1 Sprint |
| **4. Limpeza** | Remover verificações manuais de role dentro dos Services (se houver). | 1/2 Sprint |

**Métricas de Sucesso**:
*   Zero incidentes de mecânicos alterando OSs de terceiros.
*   100% das OSs finalizadas possuem `mecanicoId` preenchido.

## 7. Critérios de Aceitação

*   [ ] **Endpoint Blindado**: Tentativa de acesso de ADMIN a `iniciar-execucao` deve retornar 403.
*   [ ] **Propriedade**: Tentativa de Mecânico A alterar OS de Mecânico B deve retornar 403.
*   [ ] **Auto-atribuição**: Primeira ação do mecânico na OS grava seu ID na entidade.
*   [ ] **Testes de Integração**: `@WithMockUser` cobrindo todos os cenários da Matriz de Autorização.
*   [ ] **Log de Segurança**: Logar tentativas de acesso negado com `WARN` contendo UserID e ResourceID.
