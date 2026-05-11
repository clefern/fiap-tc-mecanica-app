package com.fiap.mecanica.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConstraintsIntegrationTest {

  @MockBean private JavaMailSender javaMailSender;

  @Autowired DataSource dataSource;

  @Test
  @DisplayName("Clientes deve possuir coluna documento e constraint única")
  void clientesDocumentoUniqueConstraintNamed() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();

      // Verify column exists
      try (ResultSet columns = metaData.getColumns(null, null, "CLIENTES", "DOCUMENTO")) {
        assertThat(columns.next()).as("Column DOCUMENTO not found in CLIENTES").isTrue();
        assertThat(columns.getString("COLUMN_NAME")).isEqualToIgnoringCase("documento");
      }

      // Verify Unique Constraint exists on DOCUMENTO
      List<String> uniqueIndices = new ArrayList<>();
      try (ResultSet indices = metaData.getIndexInfo(null, null, "CLIENTES", true, false)) {
        while (indices.next()) {
          String columnName = indices.getString("COLUMN_NAME");
          if ("documento".equalsIgnoreCase(columnName)) {
            uniqueIndices.add(indices.getString("INDEX_NAME"));
          }
        }
      }

      assertThat(uniqueIndices)
          .isNotEmpty()
          .as("Should have a unique index on 'documento' column in 'clientes' table");
    }
  }

  @Test
  @DisplayName("Veiculos deve possuir constraint única de placa")
  void veiculosPlacaUniqueConstraintNamed() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();

      // Verify Unique Constraint exists on PLACA
      List<String> uniqueIndices = new ArrayList<>();
      try (ResultSet indices = metaData.getIndexInfo(null, null, "VEICULOS", true, false)) {
        while (indices.next()) {
          String columnName = indices.getString("COLUMN_NAME");
          if ("placa".equalsIgnoreCase(columnName)) {
            uniqueIndices.add(indices.getString("INDEX_NAME"));
          }
        }
      }

      assertThat(uniqueIndices)
          .isNotEmpty()
          .as("Should have a unique index on 'placa' column in 'veiculos' table");
    }
  }
}
