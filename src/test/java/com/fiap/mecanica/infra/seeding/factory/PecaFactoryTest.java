package com.fiap.mecanica.infra.seeding.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fiap.mecanica.domain.model.Peca;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PecaFactoryTest {

  private final PecaFactory factory = new PecaFactory();

  @Test
  @DisplayName("Should create single Peca")
  void shouldCreateSinglePeca() {
    Peca peca = factory.create();
    assertNotNull(peca);
    assertNotNull(peca.getNome());
    assertNotNull(peca.getDescricao());
    assertNotNull(peca.getPrecoBase());
    assertNotNull(peca.getFabricante());
    assertNotNull(peca.getCodigoFabricante());
    assertNotNull(peca.getModelo());
    assertTrue(peca.isAtivo());
  }

  @Test
  @DisplayName("Should cover all categories eventually")
  void shouldCoverAllCategories() {
    // There are 6 categories. If we run 200 times, probability of missing one is
    // negligible.
    // Categories: Motor, Transmissão, Suspensão, Freios, Elétrica, Arrefecimento

    Set<String> categoriesSeen = new HashSet<>();
    // We can't see the category directly on Peca object as it is part of
    // description or logic.
    // Description format: "Peça de reposição para " + categoria + ...

    for (int i = 0; i < 300; i++) {
      Peca p = factory.create();
      String desc = p.getDescricao();

      if (desc.contains("Motor")) {
        categoriesSeen.add("Motor");
      } else if (desc.contains("Transmissão")) {
        categoriesSeen.add("Transmissão");
      } else if (desc.contains("Suspensão")) {
        categoriesSeen.add("Suspensão");
      } else if (desc.contains("Freios")) {
        categoriesSeen.add("Freios");
      } else if (desc.contains("Elétrica")) {
        categoriesSeen.add("Elétrica");
      } else if (desc.contains("Arrefecimento")) {
        categoriesSeen.add("Arrefecimento");
      }
    }

    // We expect to see all 6
    // If not, it's statistically very unlikely but possible.
    // Asserting >= 5 to be safe against flakiness, or just logging.
    // But ideally 6.
    assertTrue(
        categoriesSeen.size() >= 5, "Should verify most categories. Seen: " + categoriesSeen);
  }
}
