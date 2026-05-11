package com.fiap.mecanica.infra.seeding.factory;

import com.fiap.mecanica.domain.model.Peca;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class PecaFactory extends SeederFactory<Peca> {

  private static final List<String> CATEGORIAS =
      List.of("Motor", "Transmissão", "Suspensão", "Freios", "Elétrica", "Arrefecimento");

  private static final List<String> PECAS_MOTOR =
      List.of(
          "Vela de Ignição",
          "Cabo de Vela",
          "Correia Dentada",
          "Tensor da Correia",
          "Bomba D'água",
          "Filtro de Óleo",
          "Junta do Cabeçote",
          "Pistão",
          "Anéis de Pistão",
          "Válvula de Admissão");
  private static final List<String> PECAS_TRANSMISSAO =
      List.of(
          "Kit Embreagem",
          "Disco de Embreagem",
          "Platô",
          "Rolamento de Embreagem",
          "Junta Homocinética",
          "Semieixo",
          "Câmbio");
  private static final List<String> PECAS_SUSPENSAO =
      List.of(
          "Amortecedor Dianteiro",
          "Amortecedor Traseiro",
          "Mola Helicoidal",
          "Bieleta",
          "Pivô",
          "Terminal de Direção",
          "Bucha da Bandeja");
  private static final List<String> PECAS_FREIOS =
      List.of(
          "Pastilha de Freio",
          "Disco de Freio",
          "Tambor de Freio",
          "Sapata de Freio",
          "Cilindro Mestre",
          "Servo Freio",
          "Sensor ABS");
  private static final List<String> PECAS_ELETRICA =
      List.of(
          "Bateria 60Ah",
          "Alternador",
          "Motor de Arranque",
          "Bobina de Ignição",
          "Sensor de Rotação",
          "Sonda Lambda",
          "Módulo de Injeção");
  private static final List<String> PECAS_ARREFECIMENTO =
      List.of(
          "Radiador",
          "Ventoinha",
          "Válvula Termostática",
          "Mangueira Superior",
          "Reservatório de Expansão");

  private static final List<String> FABRICANTES =
      List.of(
          "Bosch",
          "NGK",
          "Brembo",
          "Monroe",
          "Cofap",
          "Valeo",
          "Magneti Marelli",
          "Moura",
          "Delphi",
          "Mahle",
          "TRW",
          "Nakata");
  private static final List<String> MODELOS_CARRO =
      List.of(
          "VW Gol",
          "Chevrolet Onix",
          "Hyundai HB20",
          "Toyota Corolla",
          "Honda Civic",
          "Jeep Renegade",
          "Fiat Strada",
          "Ford Ka");

  private final Random random = new Random();

  @Override
  public Peca create() {
    return createRandomPeca();
  }

  private Peca createRandomPeca() {
    String categoria = CATEGORIAS.get(random.nextInt(CATEGORIAS.size()));
    String nomePeca = getNomePecaPorCategoria(categoria);
    String modeloCarro = MODELOS_CARRO.get(random.nextInt(MODELOS_CARRO.size()));
    String fabricante = FABRICANTES.get(random.nextInt(FABRICANTES.size()));

    String nomeCompleto = nomePeca + " - " + modeloCarro;
    String descricao = "Peça de reposição para " + categoria + " do veículo " + modeloCarro;
    String codigo = gerarCodigo(fabricante, nomePeca);
    String modeloPeca = modeloCarro + " " + (2015 + random.nextInt(10));

    double precoBase = gerarPreco(categoria);

    return new Peca(
        null,
        nomeCompleto,
        descricao,
        BigDecimal.valueOf(precoBase),
        true,
        fabricante,
        codigo,
        modeloPeca,
        50, // Quantidade estoque inicial
        10, // Estoque mínimo inicial
        100); // Estoque máximo inicial
  }

  private String getNomePecaPorCategoria(String categoria) {
    List<String> lista =
        switch (categoria) {
          case "Motor" -> PECAS_MOTOR;
          case "Transmissão" -> PECAS_TRANSMISSAO;
          case "Suspensão" -> PECAS_SUSPENSAO;
          case "Freios" -> PECAS_FREIOS;
          case "Elétrica" -> PECAS_ELETRICA;
          case "Arrefecimento" -> PECAS_ARREFECIMENTO;
          default -> PECAS_MOTOR;
        };
    return lista.get(random.nextInt(lista.size()));
  }

  private String gerarCodigo(String fabricante, String peca) {
    return fabricante.substring(0, 3).toUpperCase(Locale.ROOT)
        + "-"
        + peca.substring(0, 3).toUpperCase(Locale.ROOT)
        + "-"
        + random.nextInt(9999);
  }

  private double gerarPreco(String categoria) {
    double min = 50.0;
    double max = 500.0;

    switch (categoria) {
      case "Motor" -> {
        min = 100.0;
        max = 2000.0;
      }
      case "Transmissão" -> {
        min = 300.0;
        max = 1500.0;
      }
      case "Suspensão" -> {
        min = 150.0;
        max = 800.0;
      }
      case "Freios" -> {
        min = 80.0;
        max = 600.0;
      }
      case "Elétrica" -> {
        min = 200.0;
        max = 1200.0;
      }
      case "Arrefecimento" -> {
        min = 100.0;
        max = 800.0;
      }
      default -> {
        // Fallback same as Motor
        min = 100.0;
        max = 2000.0;
      }
    }

    return Math.round((min + (max - min) * random.nextDouble()) * 100.0) / 100.0;
  }
}
