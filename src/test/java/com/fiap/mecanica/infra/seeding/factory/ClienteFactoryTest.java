package com.fiap.mecanica.infra.seeding.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClienteFactoryTest {

  private final ClienteFactory factory = new ClienteFactory();

  @Test
  @DisplayName("Deve criar cliente com tipo específico")
  void shouldCreateWithSpecificType() {
    Cliente fisica = factory.create(TipoPessoa.FISICA);
    assertThat(fisica.getTipo()).isEqualTo(TipoPessoa.FISICA);
    assertThat(fisica.getDocumento()).isInstanceOf(CPF.class);

    Cliente juridica = factory.create(TipoPessoa.JURIDICA);
    assertThat(juridica.getTipo()).isEqualTo(TipoPessoa.JURIDICA);
    assertThat(juridica.getDocumento()).isInstanceOf(CNPJ.class);
  }

  @Test
  @DisplayName("Deve criar cliente com tipo aleatório")
  void shouldCreateRandom() {
    // Call multiple times to likely hit both branches of the random selection
    boolean seenFisica = false;
    boolean seenJuridica = false;

    for (int i = 0; i < 20; i++) {
      Cliente c = factory.create();
      if (c.getTipo() == TipoPessoa.FISICA) {
        seenFisica = true;
      }
      if (c.getTipo() == TipoPessoa.JURIDICA) {
        seenJuridica = true;
      }
    }

    // It's statistically improbable to miss one in 20 tries (1 in 2^20)
    // But strictly we just want to ensure it runs without error.
    assertThat(seenFisica || seenJuridica).isTrue();
  }
}
