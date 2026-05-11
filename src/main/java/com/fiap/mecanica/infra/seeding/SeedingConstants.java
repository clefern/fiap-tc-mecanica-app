package com.fiap.mecanica.infra.seeding;

/** Shared constants for fixed test data used across all seeders. */
public final class SeedingConstants {

  private SeedingConstants() {}

  public static final String DEFAULT_PASSWORD = "123456";

  // Fixed admin
  public static final String FIXED_ADMIN_EMAIL = "admin@mecanica.com";

  // Fixed mecanico
  public static final String FIXED_MECANICO_EMAIL = "mecanico@teste.com";
  public static final String FIXED_MECANICO_CPF = "87903980092";

  // Fixed atendente
  public static final String FIXED_ATENDENTE_EMAIL = "atendente@teste.com";
  public static final String FIXED_ATENDENTE_CPF = "30192413244";

  // Fixed cliente
  public static final String FIXED_CLIENTE_EMAIL = "cliente@teste.com";
  public static final String FIXED_CLIENTE_CPF = "45933904279";

  // Fixed veiculo
  public static final String FIXED_VEICULO_PLACA = "ABC1D23";
}
