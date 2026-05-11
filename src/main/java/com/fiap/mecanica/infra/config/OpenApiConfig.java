package com.fiap.mecanica.infra.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "Mecânica API",
            version = "v1.0.0",
            description =
                """
    API RESTful para gestão de Oficina Mecânica.

    Recursos disponíveis:
    - Gestão de Clientes e Veículos
    - Catálogo de Serviços
    - Gestão de Mecânicos e Atendentes
    - Autenticação OAuth2 (Simulada)

    Todos os endpoints protegidos requerem token Bearer JWT.""",
            contact =
                @Contact(
                    name = "Team Mecanica",
                    email = "eng@mecanica.example",
                    url = "https://example.com/mecanica"),
            license =
                @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")),
    servers = {
      @Server(url = "http://localhost:8080", description = "Ambiente de Desenvolvimento Local"),
      @Server(url = "https://api.mecanica.example", description = "Ambiente de Produção")
    },
    security = @SecurityRequirement(name = "bearerAuth"),
    externalDocs =
        @ExternalDocumentation(
            description = "Documentação Completa (Wiki)",
            url = "https://github.com/fiap-arquitetura-soft/techChallenge/wiki"))
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Autenticação via Token JWT. Obtenha o token no endpoint /oauth/token")
public class OpenApiConfig {

  @Bean
  public OpenApiCustomizer globalOpenApiCustomizer() {
    return openApi -> {
      // Percorre todos os caminhos (paths) e operações (GET, POST, etc)
      openApi
          .getPaths()
          .values()
          .forEach(
              pathItem ->
                  pathItem
                      .readOperations()
                      .forEach(
                          operation -> {
                            ApiResponses responses = operation.getResponses();

                            // Adiciona respostas padrão se elas ainda não existirem
                            addResponseIfNotPresent(
                                responses, "400", "Dados inválidos / Requisição mal formatada");
                            addResponseIfNotPresent(
                                responses, "401", "Não autorizado / Token inválido");
                            addResponseIfNotPresent(responses, "403", "Acesso proibido");
                            addResponseIfNotPresent(responses, "500", "Erro interno do servidor");
                          }));
    };
  }

  private void addResponseIfNotPresent(ApiResponses responses, String code, String description) {
    if (!responses.containsKey(code)) {
      responses.addApiResponse(code, new ApiResponse().description(description));
    }
  }
}
