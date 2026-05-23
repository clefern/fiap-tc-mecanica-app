package com.fiap.mecanica;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class MecanicaApplicationTest {

  @MockitoBean private JavaMailSender javaMailSender;

  @Test
  @DisplayName("Deve carregar o contexto da aplicação")
  void contextLoads() {
    // Verifies that the application context loads successfully
  }
}
