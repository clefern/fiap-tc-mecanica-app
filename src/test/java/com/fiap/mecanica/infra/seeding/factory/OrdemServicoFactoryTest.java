package com.fiap.mecanica.infra.seeding.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrdemServicoFactoryTest {

  private final OrdemServicoFactory factory = new OrdemServicoFactory();

  @Test
  @DisplayName("Should create basic OS")
  void shouldCreateBasicOS() {
    OrdemServico os = factory.create();

    assertThat(os).isNotNull();
    assertThat(os.getId()).isNotNull();
    assertThat(os.getCodigo()).isNotNull();
    assertThat(os.getStatus()).isEqualTo(StatusOS.RECEBIDA);
  }

  @Test
  @DisplayName("Should create OS for client and vehicle")
  void shouldCreateOSForClientAndVehicle() {
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();

    OrdemServico os = factory.createFor(clienteId, veiculoId);

    assertThat(os).isNotNull();
    assertThat(os.getClienteId()).isEqualTo(clienteId);
    assertThat(os.getVeiculoId()).isEqualTo(veiculoId);
    assertThat(os.getStatus()).isEqualTo(StatusOS.RECEBIDA);
    assertThat(os.getCodigo()).contains("-");
    assertThat(os.getObservacoes()).isNotBlank();
  }
}
