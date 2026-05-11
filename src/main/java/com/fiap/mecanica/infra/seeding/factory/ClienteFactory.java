package com.fiap.mecanica.infra.seeding.factory;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import org.springframework.stereotype.Component;

@Component
public class ClienteFactory extends SeederFactory<Cliente> {

  @Override
  public Cliente create() {
    // Default to FISICA if not specified, or random.
    // For deterministic behavior in createMany, we might want to alternate?
    // But createMany calls this.
    // Let's make this random for generic usage.
    return create(faker.random().nextBoolean() ? TipoPessoa.FISICA : TipoPessoa.JURIDICA);
  }

  public Cliente create(TipoPessoa tipo) {
    String documento;
    if (tipo == TipoPessoa.FISICA) {
      documento = faker.cpf().valid(false);
    } else {
      documento = faker.cnpj().valid(false);
    }

    return new Cliente(
        faker.name().fullName(),
        tipo == TipoPessoa.FISICA ? CPF.of(documento) : CNPJ.of(documento),
        tipo,
        Email.of(faker.internet().emailAddress()),
        TelefoneBr.of("119" + faker.number().digits(8)),
        Endereco.of(faker.address().fullAddress()));
  }
}
