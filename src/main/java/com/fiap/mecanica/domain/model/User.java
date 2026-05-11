package com.fiap.mecanica.domain.model;

import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.valueobject.Email;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class User {

  @EqualsAndHashCode.Include protected UUID id;

  protected String nome;
  protected Email email;
  protected String password;
  protected UserRole role;
  protected boolean ativo = true;

  public void desativar() {
    this.ativo = false;
  }

  public void ativar() {
    this.ativo = true;
  }
}
