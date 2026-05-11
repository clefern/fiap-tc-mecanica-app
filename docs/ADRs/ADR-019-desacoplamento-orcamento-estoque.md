# ADR-019: Desacoplamento do Serviço de Orçamento e Criação do Serviço de Estoque

## Status
Proposto

## Contexto
Atualmente, a classe `OrcamentoServiceImpl` possui conhecimento direto sobre a implementação do controle de estoque. Durante a aprovação de um orçamento, o serviço acessa diretamente os repositórios de `Peca` e `Insumo` (`pecaRepository`, `insumoRepository`) para realizar a baixa de itens (linhas 176-225 do arquivo atual).

Esta abordagem apresenta os seguintes problemas:
1.  **Alto Acoplamento**: O serviço de orçamento conhece detalhes íntimos da persistência de estoque.
2.  **Violação de Responsabilidade Única (SRP)**: `OrcamentoService` está gerindo regras de negócio de orçamento E regras de atualização de inventário.
3.  **Dificuldade de Manutenção**: Mudanças na lógica de estoque impactam diretamente o código de orçamento.
4.  **Inconsistência Arquitetural**: O domínio de "Estoque" deveria ser um Bounded Context separado.

## Decisão
Decidimos refatorar a funcionalidade de aprovação de orçamento para remover o acoplamento direto com a infraestrutura de estoque.

### 1. Criação de um Serviço de Estoque Independente
Será criado um novo componente (contexto) dedicado exclusivamente ao controle de estoque.
*   **Responsabilidade**: Gerir a quantidade, reservas e baixas de peças e insumos.
*   **Interface**: Exporá métodos agnósticos à persistência (ex: `baixarEstoque(itemReference, quantidade)`).

### 2. Definição de Interface de Consumo (Port/Adapter)
O serviço de Orçamento consumirá o serviço de Estoque através de uma interface bem definida, seguindo o padrão de inversão de dependência.

*   **Contrato Sugerido**:
    ```java
    public interface EstoqueService {
        void baixarEstoque(UUID referenciaId, TipoItem tipo, int quantidade);
        boolean verificarDisponibilidade(UUID referenciaId, TipoItem tipo, int quantidade);
    }
    ```

### 3. Padrão de Implementação no OrcamentoService
A chamada ao novo serviço substituirá a lógica atual de repositórios, mantendo o padrão de iteração funcional já existente (identificado na linha 164 do `OrcamentoServiceImpl`).

**De (Atual):**
```java
// OrcamentoServiceImpl.java
os.getItens().forEach(this::processarBaixaEstoque); // Método privado acessa repositórios diretamente
```

**Para (Proposto):**
```java
// OrcamentoServiceImpl.java
os.getItens().forEach(item -> 
    estoqueClient.baixarEstoque(item.getReferenciaId(), item.getTipo(), item.getQuantidade())
);
```

### 4. Comunicação via Endpoints REST
Para garantir o desacoplamento físico e lógico, a comunicação entre os contextos (Orçamento -> Estoque) será projetada para ocorrer via chamadas de serviço (podendo ser implementada via Feign Client ou RestTemplate para chamadas HTTP, preparando o terreno para microsserviços).

## Consequências

### Positivas
*   **Baixo Acoplamento**: O serviço de orçamento desconhece se o estoque é um banco SQL, NoSQL ou um ERP externo.
*   **Coesão**: Regras de "Ruptura de Estoque", "Ponto de Pedido" e "Estoque Mínimo" ficam centralizadas no Serviço de Estoque.
*   **Escalabilidade**: O serviço de estoque pode ser escalado ou extraído independentemente no futuro.

### Negativas
*   **Complexidade Transacional**: Ao separar em serviços distintos (especialmente se via REST), perde-se a transação de banco de dados única (ACID). Será necessário implementar padrões de consistência eventual ou Sagas caso a operação de baixa falhe após a aprovação do orçamento (ou realizar a baixa antes da aprovação final).
*   **Latência**: Chamadas de rede (se REST) introduzem latência comparado a chamadas de método local.

## Plano de Ação
1.  Definir a API REST do Domínio de Estoque.
2.  Implementar o `EstoqueController` e `EstoqueService`.
3.  Criar o cliente HTTP (Feign) no módulo de Orçamento.
4.  Refatorar `OrcamentoServiceImpl` para usar o cliente em vez dos repositórios.
