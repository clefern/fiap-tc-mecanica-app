package com.fiap.mecanica.application.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import com.fiap.mecanica.application.email.EmailSender;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.Peca;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EstoqueAlertaEmailServiceTest {

  @Mock private EmailSender emailSender;

  @InjectMocks private EstoqueAlertaEmailService service;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(service, "adminEmail", "admin@test.com");
    ReflectionTestUtils.setField(service, "adminCopyEmail", "copy@test.com");
  }

  @Test
  @DisplayName("Deve enviar alerta de estoque baixo com sucesso")
  void shouldSendLowStockAlert() {
    Peca peca = org.mockito.Mockito.mock(Peca.class);
    Insumo insumo = org.mockito.Mockito.mock(Insumo.class);

    List<Peca> pecas = Collections.singletonList(peca);
    List<Insumo> insumos = Collections.singletonList(insumo);

    service.enviarAlertaEstoqueBaixo(pecas, insumos);

    verify(emailSender)
        .enviar(
            argThat(
                message ->
                    message.getTo().contains("admin@test.com")
                        && message.getBcc().contains("copy@test.com")
                        && message.getSubject().contains("Alerta de Estoque Baixo")
                        && message.getVariables().containsKey("pecas")
                        && message.getVariables().containsKey("insumos")
                        && message.getVariables().get("pecas").equals(pecas)
                        && message.getVariables().get("insumos").equals(insumos)));
  }

  @Test
  @DisplayName("Deve enviar alerta sem cópia se email de cópia for vazio")
  void shouldSendAlertWithoutCopyIfCopyEmailEmpty() {
    ReflectionTestUtils.setField(service, "adminCopyEmail", "");

    service.enviarAlertaEstoqueBaixo(Collections.emptyList(), Collections.emptyList());

    verify(emailSender)
        .enviar(
            argThat(
                message ->
                    message.getTo().contains("admin@test.com") && message.getBcc().isEmpty()));
  }

  @Test
  @DisplayName("Deve enviar alerta sem cópia se email de cópia for nulo")
  void shouldSendAlertWithoutCopyIfCopyEmailNull() {
    ReflectionTestUtils.setField(service, "adminCopyEmail", null);

    service.enviarAlertaEstoqueBaixo(Collections.emptyList(), Collections.emptyList());

    verify(emailSender)
        .enviar(
            argThat(
                message ->
                    message.getTo().contains("admin@test.com") && message.getBcc().isEmpty()));
  }
}
