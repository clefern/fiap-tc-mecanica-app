package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommonMapper {

  default String map(CPF cpf) {
    return cpf != null ? cpf.valor() : null;
  }

  default CPF mapCpf(String value) {
    return value != null ? CPF.of(value) : null;
  }

  default String map(CNPJ cnpj) {
    return cnpj != null ? cnpj.valor() : null;
  }

  default CNPJ mapCnpj(String value) {
    return value != null ? CNPJ.of(value) : null;
  }

  default String map(Email email) {
    return email != null ? email.value() : null;
  }

  default Email mapEmail(String value) {
    return value != null ? Email.of(value) : null;
  }

  default String map(TelefoneBr telefone) {
    return telefone != null ? telefone.value() : null;
  }

  default TelefoneBr mapTelefone(String value) {
    return value != null ? TelefoneBr.of(value) : null;
  }

  default String map(Endereco endereco) {
    return endereco != null ? endereco.value() : null;
  }

  default Endereco mapEndereco(String value) {
    return value != null ? Endereco.of(value) : null;
  }

  default String map(PlacaVeiculo placa) {
    return placa != null ? placa.value() : null;
  }

  default PlacaVeiculo mapPlaca(String value) {
    return value != null ? PlacaVeiculo.of(value) : null;
  }
}
