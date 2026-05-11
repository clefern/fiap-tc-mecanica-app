package com.fiap.mecanica.presentation.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrdemServicoRequestTest {

  private Validator validator;

  @BeforeEach
  public void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void testValidRequest() {
    OrdemServicoRequest request =
        OrdemServicoRequest.builder()
            .clienteId(UUID.randomUUID())
            .veiculoId(UUID.randomUUID())
            .observacoes("Troca de óleo")
            .build();

    Set<ConstraintViolation<OrdemServicoRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty(), "Should not have violations");
  }

  @Test
  public void testInvalidRequest() {
    OrdemServicoRequest request = new OrdemServicoRequest();

    Set<ConstraintViolation<OrdemServicoRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty(), "Should have violations");

    boolean hasClienteIdError =
        violations.stream().anyMatch(v -> v.getMessage().contains("cliente é obrigatório"));
    boolean hasVeiculoIdError =
        violations.stream().anyMatch(v -> v.getMessage().contains("veículo é obrigatório"));
    boolean hasObsError = violations.stream().anyMatch(v -> v.getMessage().contains("observações"));

    assertTrue(hasClienteIdError);
    assertTrue(hasVeiculoIdError);
    assertTrue(hasObsError);
  }
}
