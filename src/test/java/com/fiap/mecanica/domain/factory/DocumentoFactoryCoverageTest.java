package com.fiap.mecanica.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DocumentoFactoryCoverageTest {

  @Test
  @DisplayName("Deve instanciar DocumentoFactory")
  void shouldInstantiate() throws Exception {
    var constructor = DocumentoFactory.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
    } catch (InvocationTargetException e) {
      assertThat(e.getCause()).isInstanceOf(UnsupportedOperationException.class);
      assertThat(e.getCause().getMessage()).isEqualTo("Utility class");
    }
  }
}
