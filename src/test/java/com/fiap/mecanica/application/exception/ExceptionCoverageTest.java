package com.fiap.mecanica.application.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.exception.DuplicateDocumentoException;
import com.fiap.mecanica.domain.exception.DuplicatePlacaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExceptionCoverageTest {

  @Test
  @DisplayName("Deve cobrir DuplicateDocumentoException")
  void shouldCoverDuplicateDocumentoException() {
    DuplicateDocumentoException ex = new DuplicateDocumentoException("CPF duplicado");

    assertThat(ex.getMessage()).isEqualTo("Documento já cadastrado: CPF duplicado");
    assertThat(ex.getCode()).isEqualTo("DUPLICATE_DOC");
  }

  @Test
  @DisplayName("Deve cobrir DuplicatePlacaException")
  void shouldCoverDuplicatePlacaException() {
    DuplicatePlacaException ex = new DuplicatePlacaException("Placa duplicada");

    assertThat(ex.getMessage()).isEqualTo("Placa já cadastrada: Placa duplicada");
    assertThat(ex.getCode()).isEqualTo("DUPLICATE_PLACA");
  }
}
