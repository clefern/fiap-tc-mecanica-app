package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.domain.model.relatorio.RelatorioDesempenhoMecanico;
import com.fiap.mecanica.domain.model.relatorio.TempoMedioExecucaoOs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Relatórios", description = "Relatórios gerenciais — requer perfil ADMIN")
@SecurityRequirement(name = "bearerAuth")
public interface RelatorioApi {

  @Operation(
      summary = "Desempenho de mecânicos",
      description =
          "Gera um relatório com quantidade de OSs concluídas, receita total e tempo médio"
              + " de conclusão por mecânico no período informado.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Relatório gerado com sucesso",
            content =
                @Content(schema = @Schema(implementation = RelatorioDesempenhoMecanico.class))),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Requer perfil ADMIN")
      })
  ResponseEntity<List<RelatorioDesempenhoMecanico>> getDesempenhoMecanicos(
      @Parameter(description = "Data inicial (AAAA-MM-DD)", required = true) LocalDate inicio,
      @Parameter(description = "Data final (AAAA-MM-DD)", required = true) LocalDate fim);

  @Operation(
      summary = "Tempo médio de execução das OSs",
      description =
          "Calcula o tempo médio que as ordens de serviço permanecem do status APROVADA até"
              + " FINALIZADA. Considera todo o histórico sem filtro de período.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Tempo médio calculado com sucesso",
            content = @Content(schema = @Schema(implementation = TempoMedioExecucaoOs.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Requer perfil ADMIN")
      })
  ResponseEntity<TempoMedioExecucaoOs> getTempoMedioExecucaoOs();

  @Operation(
      summary = "Tempo médio de execução das OSs por período",
      description =
          "Calcula o tempo médio que as ordens de serviço permanecem do status APROVADA até"
              + " FINALIZADA dentro do período informado.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Tempo médio calculado com sucesso para o período",
            content = @Content(schema = @Schema(implementation = TempoMedioExecucaoOs.class))),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Requer perfil ADMIN")
      })
  ResponseEntity<TempoMedioExecucaoOs> getTempoMedioExecucaoOsPorPeriodo(
      @Parameter(description = "Data inicial (AAAA-MM-DD)", required = true) LocalDate inicio,
      @Parameter(description = "Data final (AAAA-MM-DD)", required = true) LocalDate fim);
}
