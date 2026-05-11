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
public class Mecanico extends User {

  private CPF cpf;
  private String especialidade;

  public Mecanico(String nome, CPF cpf, Email email, String especialidade) {
    this.nome = nome;
    this.cpf = cpf;
    this.email = email;
    this.especialidade = especialidade;
    this.ativo = true;
    this.role = UserRole.MECANICO;
    validarNome(nome);
    validarEspecialidade(especialidade);
  }

  private void validarNome(String nome) {
    if (nome == null || nome.trim().isEmpty()) {
      throw new IllegalArgumentException("Nome é obrigatório");
    }
  }

  private void validarEspecialidade(String especialidade) {
    if (especialidade == null || especialidade.trim().isEmpty()) {
      throw new IllegalArgumentException("Especialidade é obrigatória");
    }
  }
}
