package com.fiap.mecanica.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Documento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DocumentoFactoryTest {

  @Test
  @DisplayName("Deve criar CPF quando documento tiver 11 dígitos")
  void shouldCreateCPF() {
    String doc = "529.982.247-25"; // Valid CPF
    Documento result = DocumentoFactory.create(doc);

    assertThat(result).isInstanceOf(CPF.class);
    assertThat(result.valor()).isEqualTo("52998224725");
  }

  @Test
  @DisplayName("Deve criar CNPJ quando documento tiver 14 dígitos")
  void shouldCreateCNPJ() {
    String doc = "33.649.575/0001-99"; // Valid CNPJ
    Documento result = DocumentoFactory.create(doc);

    assertThat(result).isInstanceOf(CNPJ.class);
  }

  @Test
  @DisplayName("Deve lançar exceção para documento com tamanho inválido")
  void shouldThrowForInvalidLength() {
    String doc = "123";
    assertThatThrownBy(() -> DocumentoFactory.create(doc))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("inválido");
  }

  @Test
  @DisplayName("Deve lançar exceção para CPF com dígitos verificadores inválidos")
  void shouldThrowForInvalidCPFCheckDigits() {
    // Length is 11, but digits are invalid (all same digits usually invalid or specific algo check)
    String doc = "111.111.111-11";
    assertThatThrownBy(() -> DocumentoFactory.create(doc))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Deve lançar exceção para CNPJ com dígitos verificadores inválidos")
  void shouldThrowForInvalidCNPJCheckDigits() {
    // Length is 14, but digits are invalid
    String doc = "11.111.111/1111-11";
    assertThatThrownBy(() -> DocumentoFactory.create(doc))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Deve lançar exceção quando limpeza de não-dígitos resulta em tamanho inválido")
  void shouldThrowForMixedInvalidChars() {
    // "123.abc" -> "123" (length 3) -> invalid
    String doc = "123.abc";
    assertThatThrownBy(() -> DocumentoFactory.create(doc))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Deve lançar exceção para documento nulo ou vazio")
  void shouldThrowForNullOrEmpty() {
    assertThatThrownBy(() -> DocumentoFactory.create(null))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> DocumentoFactory.create(""))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
