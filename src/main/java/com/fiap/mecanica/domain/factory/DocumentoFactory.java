package com.fiap.mecanica.domain.factory;

import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Documento;

public final class DocumentoFactory {

  private DocumentoFactory() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static Documento create(String documento) {
    if (documento == null || documento.trim().isEmpty()) {
      throw new IllegalArgumentException("Documento não pode ser nulo ou vazio");
    }

    String cleaned = documento.replaceAll("\\D", "");

    if (cleaned.length() == 11) {
      return CPF.of(cleaned); // Uses the cleaning/formatting logic inside VO
    } else if (cleaned.length() == 14) {
      return CNPJ.of(cleaned);
    } else {
      throw new IllegalArgumentException(
          "Documento inválido (deve ter 11 ou 14 dígitos): " + documento);
    }
  }
}
