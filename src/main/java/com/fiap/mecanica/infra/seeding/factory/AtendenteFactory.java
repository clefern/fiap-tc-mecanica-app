package com.fiap.mecanica.infra.seeding.factory;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import org.springframework.stereotype.Component;

@Component
public class AtendenteFactory extends SeederFactory<Atendente> {

  @Override
  public Atendente create() {
    String cpf = faker.cpf().valid(false);
    return new Atendente(
        faker.name().fullName(), CPF.of(cpf), Email.of(faker.internet().emailAddress()));
  }
}
