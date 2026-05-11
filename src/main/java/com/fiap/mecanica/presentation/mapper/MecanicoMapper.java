package com.fiap.mecanica.presentation.mapper;

import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.presentation.dto.MecanicoRequest;
import com.fiap.mecanica.presentation.dto.MecanicoResponse;
import org.springframework.stereotype.Component;

@Component
public class MecanicoMapper {

  public Mecanico toDomain(MecanicoRequest request) {
    if (request == null) {
      return null;
    }
    return new Mecanico(
        request.getNome(),
        CPF.of(request.getCpf()),
        Email.of(request.getEmail()),
        request.getEspecialidade());
  }

  public MecanicoResponse toResponse(Mecanico domain) {
    if (domain == null) {
      return null;
    }
    MecanicoResponse response = new MecanicoResponse();
    response.setId(domain.getId());
    response.setNome(domain.getNome());
    response.setCpf(domain.getCpf().valor());
    response.setEmail(domain.getEmail().value());
    response.setEspecialidade(domain.getEspecialidade());
    response.setAtivo(domain.isAtivo());
    return response;
  }
}
