package com.fiap.mecanica.infra.seeding.factory;

import com.fiap.mecanica.domain.model.Insumo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class InsumoFactory extends SeederFactory<Insumo> {

  private static final List<InsumoTemplate> TEMPLATES =
      List.of(
          new InsumoTemplate("Óleo Motor 5W30", "Óleo sintético de alta performance", 45.90, "L"),
          new InsumoTemplate("Óleo Motor 10W40", "Óleo semissintético", 35.50, "L"),
          new InsumoTemplate(
              "Fluido de Freio DOT4", "Fluido para sistema de freios", 25.00, "500ml"),
          new InsumoTemplate("Aditivo Radiador", "Aditivo concentrado rosa", 18.90, "L"),
          new InsumoTemplate("Graxa Branca", "Lubrificante em spray", 15.00, "Lata"),
          new InsumoTemplate("Desengripante", "Spray multiuso", 12.00, "Lata"),
          new InsumoTemplate("Estopa", "Estopa branca para limpeza", 8.50, "kg"),
          new InsumoTemplate("Cola de Junta", "Silicone alta temperatura", 22.00, "Tubo"),
          new InsumoTemplate("Limpa Contato", "Spray para componentes elétricos", 19.90, "Lata"),
          new InsumoTemplate("Arruela de Vedação 14mm", "Arruela de cobre para bujão", 2.50, "un"),
          new InsumoTemplate("Fita Isolante", "Fita de alta fusão", 5.00, "Rolo"),
          new InsumoTemplate("Abraçadeira Metal 10-16mm", "Abraçadeira de aço carbono", 1.50, "un"),
          new InsumoTemplate("Abraçadeira Metal 20-32mm", "Abraçadeira de aço carbono", 2.00, "un"),
          new InsumoTemplate("Lâmpada H4", "Lâmpada halógena 12V 55W", 18.00, "un"),
          new InsumoTemplate("Lâmpada H7", "Lâmpada halógena 12V 55W", 22.00, "un"),
          new InsumoTemplate("Fusível 10A", "Fusível lâmina vermelho", 0.50, "un"),
          new InsumoTemplate("Fusível 15A", "Fusível lâmina azul", 0.50, "un"),
          new InsumoTemplate("Fusível 20A", "Fusível lâmina amarelo", 0.50, "un"),
          new InsumoTemplate(
              "Água Desmineralizada", "Água pura para radiador e bateria", 5.00, "L"),
          new InsumoTemplate("Fluido de Direção Hidráulica", "ATF Dextron III", 30.00, "L"),
          new InsumoTemplate("Limpa Para-brisa", "Aditivo para reservatório", 10.00, "Frasco"),
          new InsumoTemplate("Gás Refrigerante R134a", "Gás para ar condicionado", 80.00, "kg"),
          new InsumoTemplate(
              "Cola Trava Rosca", "Trava química média resistência", 25.00, "Frasco"),
          new InsumoTemplate("Lixa D'água 400", "Lixa para acabamento", 2.00, "Folha"),
          new InsumoTemplate("Lixa D'água 1200", "Lixa para polimento", 2.50, "Folha"),
          new InsumoTemplate("Massa de Polir", "Massa base d'água nº 2", 35.00, "Lata"),
          new InsumoTemplate("Cera Automotiva", "Cera protetora com carnaúba", 45.00, "Lata"),
          new InsumoTemplate("Solupan", "Desengraxante alcalino", 15.00, "L"),
          new InsumoTemplate("Intercap", "Desincrustante ácido", 15.00, "L"),
          new InsumoTemplate("Pano de Microfibra", "Pano alta absorção", 10.00, "un"));

  private final Random random = new Random();

  @Override
  public Insumo create() {
    InsumoTemplate template = TEMPLATES.get(random.nextInt(TEMPLATES.size()));
    return new Insumo(
        null,
        template.nome,
        template.descricao,
        BigDecimal.valueOf(template.preco),
        true,
        template.unidadeMedida,
        100, // Quantidade estoque inicial
        20, // Estoque mínimo inicial
        200); // Estoque máximo inicial
  }

  public List<Insumo> createAllTemplates() {
    return TEMPLATES.stream()
        .map(
            t ->
                new Insumo(
                    null,
                    t.nome,
                    t.descricao,
                    BigDecimal.valueOf(t.preco),
                    true,
                    t.unidadeMedida,
                    100,
                    20,
                    200))
        .toList();
  }

  private record InsumoTemplate(
      String nome, String descricao, double preco, String unidadeMedida) {}
}
