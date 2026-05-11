package com.fiap.mecanica.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseMigrationIntegrationTest {

  @MockBean private JavaMailSender javaMailSender;

  @Autowired JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("Flyway deve criar tabela users e migrar schema corretamente")
  void flywayShouldCreateUsersTableAndMigrateSchema() {
    // 1. Verify users table exists
    Integer usersTableCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='USERS' OR"
                + " table_name='users'",
            Integer.class);
    assertThat(usersTableCount).as("Tabela 'users' deve existir").isGreaterThan(0);

    // 2. Verify clientes table exists
    Integer clientesTableCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='CLIENTES' OR"
                + " table_name='clientes'",
            Integer.class);
    assertThat(clientesTableCount).isGreaterThan(0);

    // 3. Verify columns were dropped from clientes (nome, email moved to users)
    // In H2/Standard SQL, we check information_schema.columns
    Boolean hasNomeColumn =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) > 0 FROM information_schema.columns WHERE (table_name='CLIENTES' OR"
                + " table_name='clientes') AND (column_name='NOME' OR column_name='nome')",
            Boolean.class);
    assertThat(hasNomeColumn).as("Coluna 'nome' deve ter sido removida de 'clientes'").isFalse();

    // 4. Verify inheritance: Check if data can be inserted/queried
    Boolean hasUserType =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) > 0 FROM information_schema.columns WHERE (table_name='USERS' OR"
                + " table_name='users') AND (column_name='USER_TYPE' OR column_name='user_type')",
            Boolean.class);
    assertThat(hasUserType).isTrue();

    // 5. Verify child tables
    Integer mecanicosTableCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='MECANICOS' OR"
                + " table_name='mecanicos'",
            Integer.class);
    assertThat(mecanicosTableCount).isGreaterThan(0);
  }
}
