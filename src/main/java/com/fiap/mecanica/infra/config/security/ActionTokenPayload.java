package com.fiap.mecanica.infra.config.security;

import com.fiap.mecanica.presentation.dto.DecisaoOrcamento;
import java.util.UUID;

public record ActionTokenPayload(UUID orcamentoId, DecisaoOrcamento decisao) {}
