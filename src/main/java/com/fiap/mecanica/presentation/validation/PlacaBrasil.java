package com.fiap.mecanica.presentation.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PlacaBrasilValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface PlacaBrasil {
  String message() default "Placa inválida";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
