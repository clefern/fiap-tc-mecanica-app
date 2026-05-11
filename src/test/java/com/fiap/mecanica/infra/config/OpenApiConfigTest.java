package com.fiap.mecanica.infra.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

class OpenApiConfigTest {

  private final OpenApiConfig config = new OpenApiConfig();

  @Test
  @DisplayName("Deve adicionar respostas padrão globais")
  void shouldAddGlobalResponses() {
    OpenApiCustomizer customizer = config.globalOpenApiCustomizer();

    OpenAPI openAPI = new OpenAPI();
    Paths paths = new Paths();
    PathItem pathItem = new PathItem();
    Operation operation = new Operation();
    ApiResponses responses = new ApiResponses();

    // Adiciona uma resposta existente para garantir que não será sobrescrita
    responses.addApiResponse("200", new ApiResponse().description("OK"));

    operation.setResponses(responses);
    pathItem.setGet(operation);
    paths.addPathItem("/test", pathItem);
    openAPI.setPaths(paths);

    customizer.customise(openAPI);

    ApiResponses updatedResponses = openAPI.getPaths().get("/test").getGet().getResponses();

    assertThat(updatedResponses).containsKey("200");
    assertThat(updatedResponses.get("200").getDescription()).isEqualTo("OK");

    assertThat(updatedResponses).containsKey("400");
    assertThat(updatedResponses.get("400").getDescription())
        .isEqualTo("Dados inválidos / Requisição mal formatada");

    assertThat(updatedResponses).containsKey("401");
    assertThat(updatedResponses.get("401").getDescription())
        .isEqualTo("Não autorizado / Token inválido");

    assertThat(updatedResponses).containsKey("403");
    assertThat(updatedResponses.get("403").getDescription()).isEqualTo("Acesso proibido");

    assertThat(updatedResponses).containsKey("500");
    assertThat(updatedResponses.get("500").getDescription()).isEqualTo("Erro interno do servidor");
  }

  @Test
  @DisplayName("Não deve sobrescrever respostas existentes")
  void shouldNotOverwriteExistingResponses() {
    OpenApiCustomizer customizer = config.globalOpenApiCustomizer();

    OpenAPI openAPI = new OpenAPI();
    Paths paths = new Paths();
    PathItem pathItem = new PathItem();
    Operation operation = new Operation();
    ApiResponses responses = new ApiResponses();

    // Adiciona uma resposta 400 customizada
    responses.addApiResponse("400", new ApiResponse().description("Custom Bad Request"));

    operation.setResponses(responses);
    pathItem.setPost(operation);
    paths.addPathItem("/test", pathItem);
    openAPI.setPaths(paths);

    customizer.customise(openAPI);

    ApiResponses updatedResponses = openAPI.getPaths().get("/test").getPost().getResponses();

    assertThat(updatedResponses).containsKey("400");
    assertThat(updatedResponses.get("400").getDescription()).isEqualTo("Custom Bad Request");

    // As outras devem ser adicionadas
    assertThat(updatedResponses).containsKey("500");
  }
}
