package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.MecanicoRequest;
import com.fiap.mecanica.presentation.dto.MecanicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Mecânicos", description = "Gestão de mecânicos da oficina")
@SecurityRequirement(name = "bearerAuth")
public interface MecanicoApi {

  @Operation(
      summary = "Cadastrar novo mecânico",
      description = "Registra um novo mecânico no sistema. Requer perfil ADMIN.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Mecânico cadastrado com sucesso",
            content = @Content(schema = @Schema(implementation = MecanicoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Requer perfil ADMIN"),
        @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
      })
  ResponseEntity<MecanicoResponse> create(
      @Parameter(description = "Dados do mecânico", required = true) MecanicoRequest request);

  @Operation(
      summary = "Buscar mecânico por ID",
      description = "Retorna os detalhes de um mecânico pelo seu UUID.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Mecânico encontrado",
            content = @Content(schema = @Schema(implementation = MecanicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Mecânico não encontrado")
      })
  ResponseEntity<MecanicoResponse> getById(
      @Parameter(description = "UUID do mecânico", required = true) UUID id);

  @Operation(
      summary = "Buscar mecânico por CPF",
      description = "Busca um mecânico utilizando seu CPF.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Mecânico encontrado",
            content = @Content(schema = @Schema(implementation = MecanicoResponse.class))),
        @ApiResponse(responseCode = "400", description = "CPF inválido"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Mecânico não encontrado")
      })
  ResponseEntity<MecanicoResponse> getByCpf(
      @Parameter(
              description = "CPF do mecânico (apenas números)",
              required = true,
              example = "52998224725")
          String cpf);

  @Operation(
      summary = "Listar todos os mecânicos",
      description = "Retorna uma lista paginada de todos os mecânicos cadastrados.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de mecânicos",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<MecanicoResponse>> getAll(Pageable pageable);

  @Operation(
      summary = "Atualizar mecânico",
      description = "Atualiza as informações de um mecânico existente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Mecânico atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = MecanicoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Mecânico não encontrado")
      })
  ResponseEntity<MecanicoResponse> update(
      @Parameter(description = "UUID do mecânico", required = true) UUID id,
      @Parameter(description = "Dados atualizados", required = true) MecanicoRequest request);

  @Operation(
      summary = "Remover mecânico",
      description = "Remove um mecânico do sistema.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Mecânico removido com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Mecânico não encontrado")
      })
  ResponseEntity<Void> delete(
      @Parameter(description = "UUID do mecânico", required = true) UUID id);
}
