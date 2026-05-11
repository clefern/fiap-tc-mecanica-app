package com.fiap.mecanica.presentation.mapper;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.presentation.dto.AtendenteRequest;
import com.fiap.mecanica.presentation.dto.AtendenteResponse;
import org.springframework.stereotype.Component;

@Component
public class AtendenteMapper {

  public Atendente toDomain(AtendenteRequest request) {
    if (request == null) {
      return null;
    }
    return new Atendente(request.getNome(), CPF.of(request.getCpf()), Email.of(request.getEmail()));
  }

  public AtendenteResponse toResponse(Atendente domain) {
    if (domain == null) {
      return null;
    }
    AtendenteResponse response = new AtendenteResponse();
    response.setId(domain.getId());
    response.setNome(domain.getNome());
    response.setCpf(domain.getCpf().valor());
    response.setEmail(domain.getEmail().value());
    response.setAtivo(domain.isAtivo());
    return response;
  }
}
