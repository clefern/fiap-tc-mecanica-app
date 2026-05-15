package com.fiap.mecanica.infra.actuator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorTest {

  @MockitoBean private JavaMailSender javaMailSender;

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Deve retornar status UP no health check público")
  void shouldReturnStatusUp() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

	@Disabled
  @Test
  @DisplayName("Deve negar acesso a métricas sem autenticação")
  void shouldDenyMetricsWithoutAuth() throws Exception {
    mockMvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
  }
}
