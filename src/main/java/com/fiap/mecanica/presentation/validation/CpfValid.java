package com.fiap.mecanica.presentation.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CPFValidator.class)
@Target({FIELD, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface CpfValid {
  String message() default "CPF inválido";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
