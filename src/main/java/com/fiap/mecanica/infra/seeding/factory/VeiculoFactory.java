package com.fiap.mecanica.infra.seeding.factory;

import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import java.time.Year;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class VeiculoFactory extends SeederFactory<Veiculo> {

  @Override
  public Veiculo create() {
    // Generate random Placa in standard pattern AAA-1234 or Mercosul AAA1B23
    String placa = faker.regexify("[A-Z]{3}-[0-9]{4}");

    return new Veiculo(
        PlacaVeiculo.of(placa),
        faker.vehicle().model(),
        faker.vehicle().make(),
        faker.number().numberBetween(1990, Year.now().getValue()));
  }

  @Override
  public List<Veiculo> createMany(int count) {
    return super.createMany(count);
  }
}
