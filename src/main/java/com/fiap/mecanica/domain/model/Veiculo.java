package com.fiap.mecanica.domain.model;

import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Entidade de domínio Veiculo. Igualdade por placa. */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Veiculo {

  @Setter private UUID id;
  @EqualsAndHashCode.Include private final PlacaVeiculo placa;
  private final String modelo;
  private final String marca;
  private final int ano;

  public Veiculo(PlacaVeiculo placa, String modelo, String marca, int ano) {
    this.placa = Objects.requireNonNull(placa, "Placa obrigatória");
    this.modelo = modelo == null ? "" : modelo.trim();
    this.marca = marca == null ? "" : marca.trim();
    validateAno(ano);
    this.ano = ano;
  }

  private void validateAno(int ano) {
    int current = LocalDate.now().getYear();
    if (ano < 1900 || ano > current + 1) {
      throw new IllegalArgumentException("Ano de modelo inválido: " + ano);
    }
  }

  public boolean isAntigo() {
    int current = LocalDate.now().getYear();
    return (current - ano) > 20;
  }
}
