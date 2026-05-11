# ADR-027: Estratégia de Testes Integrados

## Status
Aceito

## Contexto
O projeto atual possui testes integrados que validam componentes de forma isolada (ex: Repositories) ou via API (Controllers), mas falta uma estratégia padronizada para verificar a integridade dos dados persistidos e a interação real entre os componentes (Banco de Dados, Serviço, Apresentação). A dúvida levantada é sobre a necessidade de queries específicas para verificar a persistência correta dos dados, além das verificações de resposta da API.

## Decisão
Adotaremos uma estratégia de testes integrados focada em **verificação ponta-a-ponta (End-to-End) dentro do contexto de teste**, garantindo que as operações de escrita sejam validadas não apenas pela resposta HTTP, mas também pelo estado final do banco de dados.

### Pilares da Estratégia

1.  **Testes de Integração de Controller (`@SpringBootTest` + `MockMvc`)**
    *   Devem continuar sendo a principal forma de validar contratos de API e fluxos de sucesso/erro.
    *   **Adição**: Sempre que uma operação de escrita (POST, PUT, DELETE, PATCH) for realizada, o teste DEVE verificar o estado do banco de dados posteriormente para garantir a persistência correta.

2.  **Verificação de Persistência**
    *   Utilizar os próprios `Repositories` (JPA) injetados no teste para consultar o estado final das entidades.
    *   Evitar queries SQL puras (`JdbcTemplate`) a menos que seja estritamente necessário para validar constraints ou migrações que o JPA abstrai.
    *   Isso garante que estamos testando a integração da aplicação como um todo, incluindo o mapeamento Objeto-Relacional.

3.  **Testes de Fluxo Completo (Cenários)**
    *   Criar testes que simulem fluxos de negócio completos (ex: Criar OS -> Aprovar -> Finalizar) em um único método de teste ou classe sequencial.
    *   Validar as transições de estado e efeitos colaterais (ex: estoque atualizado) a cada passo.

## Implementação Técnica

### Exemplo de Padrão Aceito

```java
@Test
void deveCriarOrdemServicoComSucesso() throws Exception {
    // 1. Arrange (Preparação)
    var request = criarRequestValido();

    // 2. Act (Ação)
    var result = mockMvc.perform(post("/api/ordens-servico")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    // 3. Assert Response (Validação da Resposta)
    var response = objectMapper.readValue(result.getResponse().getContentAsString(), OrdemServicoResponse.class);
    assertThat(response.getId()).isNotNull();

    // 4. Assert Persistence (Validação da Persistência - NOVO PADRÃO)
    var entityPersisted = repository.findById(response.getId());
    assertThat(entityPersisted).isPresent();
    assertThat(entityPersisted.get().getStatus()).isEqualTo(StatusOS.RECEBIDA);
    // Validar campos críticos que não necessariamente retornam na API
}
```

## Consequências
*   **Positivas**:
    *   Maior garantia de que os dados estão sendo salvos corretamente, não apenas retornados corretamente em memória.
    *   Detecção de problemas de mapeamento JPA ou transações não commitadas.
*   **Negativas**:
    *   Testes ligeiramente mais verbosos.
    *   Aumento no tempo de execução dos testes (aceitável para testes de integração).

## Referências
*   PR 10 - Testes integrados (Tech Debt)
*   Spring Boot Testing Documentation
