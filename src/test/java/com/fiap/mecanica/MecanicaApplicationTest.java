package com.fiap.mecanica;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class MecanicaApplicationTest {

  @MockBean private JavaMailSender javaMailSender;

  @Test
  @DisplayName("Deve carregar o contexto da aplicação")
  void contextLoads() {
    // Verifies that the application context loads successfully
  }
}
