# ADR-017: Fluxo de Uso E2E no Insomnia e Testes de Fluxo

## Status
Aceito

## Contexto
Atualmente, temos endpoints documentados no Swagger e exportados no Insomnia, mas eles estão isolados. Para facilitar demonstrações, validações manuais e o entendimento do ciclo de vida da Ordem de Serviço (OS), precisamos de um fluxo sequencial ("story telling") que cubra desde o cadastro do cliente até a finalização do serviço.

Além disso, é necessário avaliar a viabilidade de implementar testes automatizados desse fluxo (E2E) diretamente em Java, para garantir que as integrações entre os componentes (Controller -> Service -> DB) e as transições de estado da OS funcionem corretamente.

## Decisão

### 1. Criação do Fluxo Sequencial no Insomnia
Será criada uma pasta dedicada no Insomnia chamada **"Fluxo Completo (E2E)"**, contendo as requisições ordenadas e configuradas para encadear variáveis (Chain Requests).

#### Variáveis de Ambiente Necessárias (Base Environment)
- `base_url`: `http://localhost:8080`
- `email_atendente`: `atendente@teste.com`
- `pass_atendente`: `123456`
- `email_mecanico`: `mecanico@teste.com`
- `pass_mecanico`: `123456`
- `email_cliente`: `(gerado dinamicamente)`
- `pass_cliente`: `123456`

#### Sequência de Requisições

#### 1. Cadastro e Abertura (Atendente)
1.  **Login Atendente**
    - **Endpoint**: `POST /oauth/token`
    - **Body**: `{ "grant_type": "password", "username": "{{ email_atendente }}", "password": "{{ pass_atendente }}" }`
    - **Ação**: Capturar `access_token` em variável de ambiente `token_atendente`.
2.  **Cadastrar Cliente**
    - **Endpoint**: `POST /api/clientes`
    - **Auth**: Bearer `token_atendente`
    - **Body**: JSON com dados do cliente (CPF único).
    - **Ação**: Capturar `id` em `cliente_id` e `email` em `email_cliente`.
3.  **Cadastrar Veículo**
    - **Endpoint**: `POST /api/clientes/{{ cliente_id }}/veiculos`
    - **Auth**: Bearer `token_atendente`
    - **Body**: JSON com dados do veículo (Placa única).
    - **Ação**: Capturar `id` em `veiculo_id`.
4.  **Criar Ordem de Serviço**
    - **Endpoint**: `POST /api/ordens-servico`
    - **Auth**: Bearer `token_atendente`
    - **Body**: `{ "clienteId": "{{ cliente_id }}", "veiculoId": "{{ veiculo_id }}", "observacoes": "Barulho no motor" }`
    - **Ação**: Capturar `id` em `os_id`.

#### 2. Triagem e Orçamento (Mecânico)
5.  **Login Mecânico**
    - **Endpoint**: `POST /oauth/token`
    - **Body**: `{ "grant_type": "password", "username": "{{ email_mecanico }}", "password": "{{ pass_mecanico }}" }`
    - **Ação**: Setar `token_mecanico`.
6.  **Listar Fila de Orçamento**
    - **Endpoint**: `GET /api/ordens-servico/fila-orcamento`
    - **Auth**: Bearer `token_mecanico`
    - **Objetivo**: Validar que a `os_id` aparece na lista.
7.  **Consultar OS**
    - **Endpoint**: `GET /api/ordens-servico/{{ os_id }}`
    - **Auth**: Bearer `token_mecanico`
8.  **Iniciar Diagnóstico**
    - **Endpoint**: `POST /api/ordens-servico/{{ os_id }}/acoes/iniciar-diagnostico`
    - **Auth**: Bearer `token_mecanico`
9.  **Adicionar Peça (Item)**
    - **Endpoint**: `POST /api/ordens-servico/{{ os_id }}/itens`
    - **Auth**: Bearer `token_mecanico`
    - **Body**: `{ "descricao": "Oleo 5w30", "valorUnitario": 50.00, "quantidade": 4, "tipo": "PECA" }`
10. **Adicionar Serviço (Item)**
    - **Endpoint**: `POST /api/ordens-servico/{{ os_id }}/itens`
    - **Auth**: Bearer `token_mecanico`
    - **Body**: `{ "descricao": "Troca de Oleo", "valorUnitario": 100.00, "quantidade": 1, "tipo": "SERVICO" }`
11. **Emitir Orçamento (Aguardando Aprovação)**
    - **Endpoint**: `POST /api/ordens-servico/{{ os_id }}/acoes/emitir-orcamento`
    - **Nota**: Este endpoint precisa ser exposto no `OrdemServicoAcoesController` (método `emitirOrcamento` do Service).
    - **Auth**: Bearer `token_mecanico`

#### 3. Aprovação (Cliente)
12. **Login Cliente**
    - **Endpoint**: `POST /oauth/token`
    - **Body**: `{ "grant_type": "password", "username": "{{ email_cliente }}", "password": "{{ pass_cliente }}" }`
    - **Ação**: Setar `token_cliente`.
13. **Consultar Orçamento**
    - **Endpoint**: `GET /api/orcamentos/os/{{ os_id }}`
    - **Auth**: Bearer `token_cliente`
14. **Aprovar Orçamento**
    - **Endpoint**: `POST /api/ordens-servico/{{ os_id }}/acoes/aprovar`
    - **Auth**: Bearer `token_cliente`

#### 4. Execução e Entrega (Mecânico)
15. **Login Mecânico** (Reuso do token existente ou novo login)
16. **Listar Fila de Execução**
    - **Endpoint**: `GET /api/ordens-servico/fila-execucao`
    - **Auth**: Bearer `token_mecanico`
17. **Iniciar Execução**
    - **Endpoint**: `POST /api/ordens-servico/{{ os_id }}/acoes/iniciar-execucao`
    - **Auth**: Bearer `token_mecanico`
18. **Finalizar Serviço**
    - **Endpoint**: `POST /api/ordens-servico/{{ os_id }}/acoes/finalizar`
    - **Auth**: Bearer `token_mecanico`

---

### 2. Estudo de Viabilidade: Testes de Fluxo em Java

Para implementar testes que simulem esse mesmo fluxo de uso (Story Telling) dentro da pipeline de CI/CD, a recomendação é utilizar **RestAssured** em conjunto com **JUnit 5** e **Spring Boot Test**.

#### Ferramentas Recomendadas
1.  **RestAssured**:
    - **Por que?**: DSL fluente, excelente para testar APIs REST, validação simples de status code e corpo de resposta (JSON), fácil extração de dados para encadeamento (ex: `extract().path("id")`).
    - **Integração**: Funciona perfeitamente com `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
2.  **Testcontainers**:
    - **Por que?**: Garante um banco de dados limpo e isolado (PostgreSQL) para cada execução de teste, evitando sujeira de dados entre testes de fluxo.

#### Exemplo de Abordagem
Criar uma classe de teste `FluxoCompletoE2ETest.java` anotada com `@TestMethodOrder(OrderAnnotation.class)` para garantir a sequência (embora testes devam ser independentes, testes de fluxo E2E são, por definição, sequenciais em sua lógica de negócio). Ou, preferencialmente, um único método de teste grande (`@Test void fluxoCompletoDeOrdemServico()`) que executa os passos sequencialmente, mantendo o estado em variáveis locais.

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class FluxoCompletoE2ETest {

    @Test
    void fluxoCompleto() {
        // 1. Cadastrar Cliente
        String clienteId = given()...post("/api/clientes").then().extract().path("id");

        // 2. Cadastrar Veiculo
        String veiculoId = given()...post(...).then().extract().path("id");

        // 3. Criar OS
        String osId = given().body(new OsRequest(clienteId, veiculoId))...post("/api/ordens-servico")...

        // ... e assim por diante
    }
}
```

#### Conclusão do Estudo
A implementação é **altamente viável** e recomendada. Ela traz segurança de que o fluxo de negócio principal (Core Business) não foi quebrado por alterações em componentes isolados. Deve ser tratada como um teste de integração de "fumaça" (Smoke Test) ou E2E leve.

## Consequências
- **Positivo**: Demonstrações mais ágeis; Validação real do negócio; Documentação executável.
- **Positivo**: Testes em Java garantirão que refatorações não quebrem o fluxo do usuário.
- **Negativo**: Manutenção do arquivo Insomnia manual (deve ser atualizado se a API mudar).
- **Ação Necessária**: Implementar o endpoint `emitirOrcamento` no controller, que foi identificado como faltante durante a análise.
