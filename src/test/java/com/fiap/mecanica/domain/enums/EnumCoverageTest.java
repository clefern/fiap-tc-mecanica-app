package com.fiap.mecanica.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnumCoverageTest {

  @Test
  @DisplayName("Deve cobrir TipoPessoa")
  void shouldCoverTipoPessoa() {
    for (TipoPessoa tipo : TipoPessoa.values()) {
      assertThat(TipoPessoa.valueOf(tipo.name())).isEqualTo(tipo);
    }
  }

  @Test
  @DisplayName("Deve cobrir UserRole")
  void shouldCoverUserRole() {
    for (UserRole role : UserRole.values()) {
      assertThat(UserRole.valueOf(role.name())).isEqualTo(role);
    }
  }

  @Test
  @DisplayName("Deve cobrir StatusEstoque")
  void shouldCoverStatusEstoque() {
    for (StatusEstoque status : StatusEstoque.values()) {
      assertThat(StatusEstoque.valueOf(status.name())).isEqualTo(status);
    }
  }

  @Test
  @DisplayName("Deve cobrir Prioridade")
  void shouldCoverPrioridade() {
    for (Prioridade prioridade : Prioridade.values()) {
      assertThat(Prioridade.valueOf(prioridade.name())).isEqualTo(prioridade);
    }
  }

  @Test
  @DisplayName("Deve cobrir StatusOrcamento")
  void shouldCoverStatusOrcamento() {
    for (StatusOrcamento status : StatusOrcamento.values()) {
      assertThat(StatusOrcamento.valueOf(status.name())).isEqualTo(status);
    }
  }

  @Test
  @DisplayName("Deve cobrir TipoItem")
  void shouldCoverTipoItem() {
    for (TipoItem tipo : TipoItem.values()) {
      assertThat(TipoItem.valueOf(tipo.name())).isEqualTo(tipo);
    }
  }

  @Test
  @DisplayName("Deve cobrir StatusOS")
  void shouldCoverStatusOS() {
    for (StatusOS status : StatusOS.values()) {
      assertThat(StatusOS.valueOf(status.name())).isEqualTo(status);
    }
  }

  @Test
  @DisplayName("Deve cobrir CategoriaServico")
  void shouldCoverCategoriaServico() {
    for (CategoriaServico categoria : CategoriaServico.values()) {
      assertThat(CategoriaServico.valueOf(categoria.name())).isEqualTo(categoria);
    }
  }
}
