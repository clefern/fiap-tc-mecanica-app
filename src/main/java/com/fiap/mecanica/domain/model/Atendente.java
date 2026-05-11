package com.fiap.mecanica.domain.model;

import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Atendente extends User {

  private CPF cpf;

  public Atendente(String nome, CPF cpf, Email email) {
    this.nome = nome;
    this.cpf = cpf;
    this.email = email;
    this.ativo = true;
    this.role = UserRole.ATENDENTE;
    validarNome(nome);
  }

  private void validarNome(String nome) {
    if (nome == null || nome.trim().isEmpty()) {
      throw new IllegalArgumentException("Nome não pode ser vazio");
    }
  }
}
