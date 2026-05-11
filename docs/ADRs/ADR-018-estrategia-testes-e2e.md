# ADR-018: Estratégia de Testes End-to-End (E2E)

## Status
Aceito

## Contexto
O sistema Mecânica está evoluindo em complexidade, com fluxos de negócio que atravessam múltiplas camadas (Controller, Service, Repository) e dependências de infraestrutura (Banco de Dados). Atualmente, possuímos:
1.  **Testes Unitários**: Alta cobertura, focados em lógica isolada.
2.  **Testes de Integração (`@SpringBootTest`)**: Validam componentes interagindo com o banco H2/Testcontainers, mas muitas vezes focam em "fatias" do sistema (ex: apenas Repository ou Service).
3.  **Testes Manuais (Insomnia)**: Utilizamos coleções do Insomnia para validar fluxos completos manualmente.

**Problema**: A validação manual via Insomnia é propensa a erro humano e não escala. Testes de integração atuais, embora úteis, muitas vezes não garantem que a aplicação funcione como uma "caixa preta" do ponto de vista do cliente HTTP (serialização JSON, filtros de segurança, headers, códigos HTTP corretos).

Precisamos de uma estratégia automatizada para validar **Jornadas Críticas do Usuário (Critical User Journeys - CUJs)** de ponta a ponta.

## Decisão
Adotaremos **RestAssured** em conjunto com **JUnit 5** e **Testcontainers** como stack padrão para Testes E2E.

### Tecnologias Avaliadas

| Tecnologia | Veredito | Justificativa |
| :--- | :--- | :--- |
| **Postman / Newman** | Rejeitado | Difícil manutenção e versionamento (JSONs gigantes); separação entre código da aplicação e código de teste. |
| **Cucumber (BDD)** | Rejeitado | Adiciona camada de complexidade (Gherkin) desnecessária para o tamanho atual do time; overhead de manutenção dos "glue codes". |
| **RestAssured (Java)** | **Escolhido** | **Nativo Java**: Refatoração segura junto com o código.<br>**DSL Fluente**: Legível e expressivo (`given().when().then()`).<br>**Integração**: Funciona dentro do ciclo Maven (`verify` phase). |
| **Playwright / Cypress** | Rejeitado (Backend) | Excelentes para UI, mas overkill para testes puramente de API Backend neste momento. |

### Padrões e Melhores Práticas Definidas

1.  **Separação de Suítes**:
    *   Testes E2E devem rodar separadamente dos unitários.
    *   Uso do `maven-failsafe-plugin` para rodar testes com sufixo `IT` (Integration Test) ou `E2E`.
2.  **Ambiente Realista**:
    *   Usar `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)` para subir o servidor real.
    *   Usar **Testcontainers** para subir PostgreSQL idêntico à produção (nada de H2 para E2E).
3.  **Gerenciamento de Dados**:
    *   **Limpeza**: O banco deve ser limpo antes/depois de cada teste (ou usar transações com rollback, embora rollback esconda bugs de commit).
    *   **Factories**: Reutilizar as Factories existentes (ex: `ClienteFactory`) para massa de dados.
4.  **Escopo**:
    *   Focar nos "Caminhos Felizes" críticos e principais fluxos de exceção de negócio.
    *   Evitar testar cada validação de campo (deixar isso para unitários).

## Consequências

### Positivas
*   **Confiança no Deploy**: Garantia que os endpoints REST respondem corretamente aos contratos.
*   **Documentação Viva**: Os testes documentam como a API deve ser consumida.
*   **Refatoração Segura**: Podemos alterar toda a implementação interna; se o contrato da API mudar, o teste quebra.

### Negativas
*   **Tempo de Execução**: Testes E2E são lentos (sobem contexto Spring + Docker). Devem rodar na pipeline de CI, não necessariamente a cada "save" local.
*   **Instabilidade (Flakiness)**: Maior risco de falhas por timeout ou estado de banco sujo se não gerenciado corretamente.

## Plano de Implementação (Exemplo)

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class FluxoOrdemServicoE2E {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        // limpar banco
    }

    @Test
    void deveCriarEFinalizarOS() {
        // 1. Criar Cliente
        String clienteId = given()
            .contentType(ContentType.JSON)
            .body(clienteJson)
        .when()
            .post("/api/clientes")
        .then()
            .statusCode(201)
            .extract().path("id");

        // 2. Criar OS para o Cliente
        // ...
    }
}
```

## Cobertura Atual e Metas
*   **Atual**: 0% automatizado (100% manual via Insomnia).
*   **Meta Curto Prazo**: Automatizar o fluxo "Criação de OS até Aprovação".
*   **Meta Médio Prazo**: Cobrir todos os fluxos do `insomnia_export.json`.
