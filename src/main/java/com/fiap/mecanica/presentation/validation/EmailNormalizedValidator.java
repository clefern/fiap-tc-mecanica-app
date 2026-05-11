package com.fiap.mecanica.presentation.validation;

import com.fiap.mecanica.domain.valueobject.Email;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailNormalizedValidator implements ConstraintValidator<EmailNormalized, String> {
  @Override
  public void initialize(EmailNormalized constraintAnnotation) {
    /* no-op */
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    } // combine with @NotNull when presence is required
    try {
      Email.of(value);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
