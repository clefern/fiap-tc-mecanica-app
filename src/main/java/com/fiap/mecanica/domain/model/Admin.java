package com.fiap.mecanica.domain.model;

import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.valueobject.Email;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Admin extends User {

  public Admin(String nome, Email email, String password) {
    this.nome = nome;
    this.email = email;
    this.password = password;
    this.ativo = true;
    this.role = UserRole.ADMIN;
  }
}
