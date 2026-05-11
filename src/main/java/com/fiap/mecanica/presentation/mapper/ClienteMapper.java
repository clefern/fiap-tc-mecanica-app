package com.fiap.mecanica.presentation.mapper;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.valueobject.*;
import com.fiap.mecanica.presentation.dto.ClienteRequest;
import com.fiap.mecanica.presentation.dto.ClienteResponse;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

  public Cliente toDomain(ClienteRequest request) {
    if (request == null) {
      return null;
    }

    Documento doc;
    if (request.getTipoPessoa() == TipoPessoa.FISICA) {
      doc = CPF.of(request.getDocumento());
    } else {
      doc = new CNPJ(request.getDocumento());
    }

    return new Cliente(
        request.getNome(),
        doc,
        request.getTipoPessoa(),
        Email.of(request.getEmail()),
        TelefoneBr.of(request.getTelefone()),
        Endereco.of(request.getEndereco()));
  }

  public ClienteResponse toResponse(Cliente cliente) {
    if (cliente == null) {
      return null;
    }

    ClienteResponse dto = new ClienteResponse();
    dto.setId(cliente.getId());
    dto.setNome(cliente.getNome());
    dto.setDocumento(cliente.getDocumento().valor());
    dto.setTipoPessoa(cliente.getTipo());
    dto.setEmail(cliente.getEmail().value());
    dto.setTelefone(cliente.getTelefone().value());
    dto.setEndereco(cliente.getEndereco().value());
    return dto;
  }
}
