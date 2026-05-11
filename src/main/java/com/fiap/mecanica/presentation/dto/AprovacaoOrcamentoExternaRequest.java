package com.fiap.mecanica.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AprovacaoOrcamentoExternaRequest(
    @NotBlank(message = "Código da OS é obrigatório") String osCodigo,
    @NotNull(message = "Decisão é obrigatória") DecisaoOrcamento decisao) {}
