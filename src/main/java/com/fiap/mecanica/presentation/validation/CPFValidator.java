package com.fiap.mecanica.presentation.validation;

import com.fiap.mecanica.domain.valueobject.CPF;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CpfValid, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    } // use @NotNull to enforce presence
    try {
      CPF.of(value);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
