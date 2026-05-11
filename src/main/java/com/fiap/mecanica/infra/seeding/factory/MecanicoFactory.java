package com.fiap.mecanica.infra.seeding.factory;

import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import org.springframework.stereotype.Component;

@Component
public class MecanicoFactory extends SeederFactory<Mecanico> {

  private static final java.util.List<String> ESPECIALIDADES =
      java.util.List.of(
          "Mecânica Geral",
          "Mecânica de Motor",
          "Suspensão e Direção",
          "Freios e ABS",
          "Elétrica Automotiva",
          "Injeção Eletrônica",
          "Ar Condicionado",
          "Transmissão e Câmbio",
          "Escapamentos",
          "Retífica de Motores",
          "Funilaria e Pintura",
          "Vidraçaria Automotiva",
          "Tapeçaria Automotiva",
          "Sistemas Hidráulicos",
          "Diagnóstico Computadorizado");

  @Override
  public Mecanico create() {
    String cpf = faker.cpf().valid(false);
    String especialidade = ESPECIALIDADES.get(faker.random().nextInt(ESPECIALIDADES.size()));

    return new Mecanico(
        faker.name().fullName(),
        CPF.of(cpf),
        Email.of(faker.internet().emailAddress()),
        especialidade);
  }
}
