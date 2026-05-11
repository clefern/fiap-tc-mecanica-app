package com.fiap.mecanica.presentation.validation;

import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelefoneValidator implements ConstraintValidator<Telefone, String> {
  @Override
  public void initialize(Telefone constraintAnnotation) {
    /* no-op */
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    } // compose with @NotNull when needed
    try {
      TelefoneBr.of(value);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
