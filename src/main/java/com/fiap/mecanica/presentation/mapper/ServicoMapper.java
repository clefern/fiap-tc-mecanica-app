package com.fiap.mecanica.presentation.mapper;

import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.presentation.dto.ServicoRequest;
import com.fiap.mecanica.presentation.dto.ServicoResponse;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class ServicoMapper {

  public Servico toDomain(ServicoRequest request) {
    if (request == null) {
      return null;
    }
    return new Servico(
        request.getNome(),
        request.getDescricao(),
        request.getValorBase(),
        Duration.ofMinutes(request.getTempoEstimadoMinutos()),
        request.getCategoria());
  }

  public ServicoResponse toResponse(Servico servico) {
    if (servico == null) {
      return null;
    }
    return new ServicoResponse(
        servico.getId(),
        servico.getNome(),
        servico.getDescricao(),
        servico.getPrecoBase(),
        servico.getTempoEstimado().toMinutes(),
        servico.getCategoria(),
        servico.isAtivo());
  }
}
