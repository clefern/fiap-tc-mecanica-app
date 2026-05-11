package com.fiap.mecanica.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TrocarMecanicoRequest(
    @NotNull(message = "O ID do novo mecânico é obrigatório") UUID novoMecanicoId) {}
