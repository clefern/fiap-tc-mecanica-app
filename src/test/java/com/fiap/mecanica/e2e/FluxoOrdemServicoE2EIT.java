package com.fiap.mecanica.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.MecanicaApplication;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.infra.seeding.SeedingOrchestrator;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Disabled
@SpringBootTest(
    classes = MecanicaApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FluxoOrdemServicoE2EIT {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("mecanica_e2e")
          .withUsername("mecanica_user")
          .withPassword("mecanica_pass");

  @LocalServerPort int port;

  @Autowired private SeedingOrchestrator seedingOrchestrator;
  @Autowired private ClienteRepository clienteRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @DynamicPropertySource
  static void configureDataSource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.flyway.enabled", () -> true);
  }

  @BeforeAll
  void seedOnce() {
    // Evita rodar seed pesado antes de cada teste E2E.
    seedingOrchestrator.seed();
  }

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
    RestAssured.basePath = "/";
  }

  @Test
  @DisplayName("Fluxo completo: criar OS, emitir orçamento, aprovar, executar e entregar")
  void deveCriarEmitirOrcamentoEAprovarOrdemServico() throws IOException {
    String placaVeiculo = gerarPlacaMercosulAleatoria();

    // 1. Login como Atendente
    String atendenteToken = obterToken("password", "atendente@teste.com", "123456");

    // 2. Criar Cliente (Atendente)
    UUID clienteId =
        UUID.fromString(
            given()
                .auth()
                .oauth2(atendenteToken)
                .contentType(ContentType.JSON)
                .body(
                    "{\"nome\":\"Cliente"
                        + " E2E\",\"cpf\":\"39053344705\",\"email\":\"cliente.e2e@test.com\"}")
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id"));

    // 2.1. Resetar senha do cliente criado para permitir login (Workaround para
    // teste)
    // No fluxo real, o cliente receberia a senha por email.
    Cliente cliente = clienteRepository.findById(clienteId).orElseThrow();
    cliente.setPassword(passwordEncoder.encode("123456"));
    clienteRepository.save(cliente);

    // 3. Criar Veículo (Atendente)
    UUID veiculoId =
        UUID.fromString(
            given()
                .auth()
                .oauth2(atendenteToken)
                .contentType(ContentType.JSON)
                .body(
                    "{\"clienteId\":\""
                        + clienteId
                        + "\",\"placa\":\""
                        + placaVeiculo
                        + "\",\"modelo\":\"Modelo E2E\"}")
                .when()
                .post("/api/veiculos")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id"));

    // 4. Criar Ordem de Serviço (Atendente)
    String osIdString =
        given()
            .auth()
            .oauth2(atendenteToken)
            .contentType(ContentType.JSON)
            .body(
                "{\"clienteId\":\""
                    + clienteId
                    + "\",\"veiculoId\":\""
                    + veiculoId
                    + "\",\"observacoes\":\"OS E2E\"}")
            .when()
            .post("/api/ordens-servico")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .extract()
            .path("id");

    UUID osId = UUID.fromString(osIdString);

    // 5. Login como Mecânico
    String mecanicoToken = obterToken("password", "mecanico@teste.com", "123456");

    // 6. Mecânico inicia diagnóstico (assume a OS)
    given()
        .auth()
        .oauth2(mecanicoToken)
        .when()
        .post("/api/ordens-servico/" + osId + "/acoes/iniciar-diagnostico")
        .then()
        .statusCode(200)
        .body("status", equalTo("EM_DIAGNOSTICO"));

    // 7. Mecânico adiciona itens (Serviço)
    given()
        .auth()
        .oauth2(mecanicoToken)
        .contentType(ContentType.JSON)
        .body(
            "{\"tipo\":\"SERVICO\",\"descricao\":\"Diagnostico"
                + " E2E\",\"valorUnitario\":100.0,\"quantidade\":1}")
        .when()
        .post("/api/ordens-servico/" + osId + "/itens")
        .then()
        .statusCode(200)
        .body("itens.size()", equalTo(1));

    // 8. Mecânico emite orçamento
    given()
        .auth()
        .oauth2(mecanicoToken)
        .when()
        .post("/api/ordens-servico/" + osId + "/acoes/emitir-orcamento")
        .then()
        .statusCode(200)
        .body("status", equalTo("AGUARDANDO_APROVACAO"));

    // 9. Login como Cliente (recém-criado)
    String clienteToken = obterToken("password", "cliente.e2e@test.com", "123456");

    // 10. Cliente aprova orçamento (via OrcamentoController)
    given()
        .auth()
        .oauth2(clienteToken)
        .when()
        .post("/api/orcamentos/os/" + osId + "/aprovar")
        .then()
        .statusCode(200)
        .body("status", equalTo("APROVADO")); // Status do Orçamento

    // 10.1 Verificar status da OS (deve ter mudado para APROVADA via evento)
    given()
        .auth()
        .oauth2(atendenteToken)
        .when()
        .get("/api/ordens-servico/" + osId)
        .then()
        .statusCode(200)
        .body("status", equalTo("APROVADA"));

    // 11. Mecânico inicia execução
    given()
        .auth()
        .oauth2(mecanicoToken)
        .when()
        .post("/api/ordens-servico/" + osId + "/acoes/iniciar-execucao")
        .then()
        .statusCode(200)
        .body("status", equalTo("EM_EXECUCAO"));

    // 12. Mecânico finaliza execução
    given()
        .auth()
        .oauth2(mecanicoToken)
        .when()
        .post("/api/ordens-servico/" + osId + "/acoes/finalizar")
        .then()
        .statusCode(200)
        .body("status", equalTo("FINALIZADA"));

    // 13. Atendente entrega veículo
    given()
        .auth()
        .oauth2(atendenteToken)
        .when()
        .post("/api/ordens-servico/" + osId + "/acoes/entregar")
        .then()
        .statusCode(200)
        .body("status", equalTo("ENTREGUE"));
  }

  @Test
  @DisplayName("Abertura completa de OS: cria OS com item de serviço em uma única requisição")
  void deveAbrirOsCompletaComItemDeServico() throws IOException {
    String placaVeiculo = gerarPlacaMercosulAleatoria();

    // 1. Login como Atendente
    String atendenteToken = obterToken("password", "atendente@teste.com", "123456");

    // 2. Criar Cliente
    UUID clienteId =
        UUID.fromString(
            given()
                .auth()
                .oauth2(atendenteToken)
                .contentType(ContentType.JSON)
                .body(
                    "{\"nome\":\"Cliente Abertura\",\"cpf\":\"15350946053\","
                        + "\"email\":\"cliente.abertura@test.com\"}")
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(201)
                .extract()
                .path("id"));

    // 3. Criar Veículo
    UUID veiculoId =
        UUID.fromString(
            given()
                .auth()
                .oauth2(atendenteToken)
                .contentType(ContentType.JSON)
                .body(
                    "{\"clienteId\":\""
                        + clienteId
                        + "\",\"placa\":\""
                        + placaVeiculo
                        + "\",\"modelo\":\"Modelo Abertura\"}")
                .when()
                .post("/api/veiculos")
                .then()
                .statusCode(201)
                .extract()
                .path("id"));

    // 4. Abertura completa da OS com um item de serviço
    given()
        .auth()
        .oauth2(atendenteToken)
        .contentType(ContentType.JSON)
        .body(
            "{"
                + "\"clienteId\":\""
                + clienteId
                + "\","
                + "\"veiculoId\":\""
                + veiculoId
                + "\","
                + "\"observacoes\":\"Revisão completa\","
                + "\"itens\":[{"
                + "\"tipo\":\"SERVICO\","
                + "\"descricao\":\"Revisão geral\","
                + "\"valorUnitario\":150.0,"
                + "\"quantidade\":1,"
                + "\"referenciaId\":\""
                + UUID.randomUUID()
                + "\""
                + "}]"
                + "}")
        .when()
        .post("/api/ordens-servico/abertura-completa")
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("status", equalTo("RECEBIDA"))
        .body("itens", hasSize(1))
        .body("itens[0].descricao", equalTo("Revisão geral"))
        .body("valorTotal", greaterThan(0f));
  }

  private String obterToken(String grantType, String username, String password) throws IOException {
    String responseBody =
        given()
            .contentType(ContentType.JSON)
            .body(
                "{\"grant_type\":\""
                    + grantType
                    + "\",\"username\":\""
                    + username
                    + "\",\"password\":\""
                    + password
                    + "\"}")
            .when()
            .post("/oauth/token")
            .then()
            .statusCode(200)
            .extract()
            .asString();

    JsonNode node = objectMapper.readTree(responseBody);
    return node.get("access_token").asText();
  }

  private String gerarPlacaMercosulAleatoria() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    return ""
        + letraAleatoria(random)
        + letraAleatoria(random)
        + letraAleatoria(random)
        + random.nextInt(10)
        + letraAleatoria(random)
        + random.nextInt(10)
        + random.nextInt(10);
  }

  private char letraAleatoria(ThreadLocalRandom random) {
    return (char) ('A' + random.nextInt(26));
  }
}
