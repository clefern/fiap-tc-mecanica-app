package com.fiap.mecanica.presentation.validation;

import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PlacaBrasilValidator implements ConstraintValidator<PlacaBrasil, String> {
  @Override
  public void initialize(PlacaBrasil constraintAnnotation) {
    /* no-op */
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    } // combine with @NotNull when presence is required
    try {
      PlacaVeiculo.of(value);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
