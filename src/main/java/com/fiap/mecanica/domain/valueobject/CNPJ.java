package com.fiap.mecanica.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class CNPJ implements Documento {
  private final String numero;

  public CNPJ(String numero) {
    if (numero == null) {
      throw new IllegalArgumentException("CNPJ não pode ser nulo");
    }
    String limpo = numero.replaceAll("\\D", "");
    if (!isValido(limpo)) {
      throw new IllegalArgumentException("CNPJ inválido: " + numero);
    }
    this.numero = limpo;
  }

  public static CNPJ of(String numero) {
    return new CNPJ(numero);
  }

  @Override
  public String valor() {
    return numero;
  }

  @Override
  public String formatado() {
    return numero.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
  }

  private boolean isValido(String cnpj) {
    if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) {
      return false;
    }

    char dig13;
    char dig14;
    int sm;
    int i;
    int r;
    int num;
    int peso;

    // Calculo do 1o. Digito Verificador
    sm = 0;
    peso = 2;
    for (i = 11; i >= 0; i--) {
      num = (int) (cnpj.charAt(i) - 48);
      sm = sm + (num * peso);
      peso = peso + 1;
      if (peso == 10) {
        peso = 2;
      }
    }

    r = sm % 11;
    if ((r == 0) || (r == 1)) {
      dig13 = '0';
    } else {
      dig13 = (char) ((11 - r) + 48);
    }

    // Calculo do 2o. Digito Verificador
    sm = 0;
    peso = 2;
    for (i = 12; i >= 0; i--) {
      num = (int) (cnpj.charAt(i) - 48);
      sm = sm + (num * peso);
      peso = peso + 1;
      if (peso == 10) {
        peso = 2;
      }
    }

    r = sm % 11;
    if ((r == 0) || (r == 1)) {
      dig14 = '0';
    } else {
      dig14 = (char) ((11 - r) + 48);
    }

    return (dig13 == cnpj.charAt(12)) && (dig14 == cnpj.charAt(13));
  }
}
