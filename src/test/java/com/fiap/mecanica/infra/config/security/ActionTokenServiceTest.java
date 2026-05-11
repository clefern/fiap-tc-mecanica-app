package com.fiap.mecanica.infra.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.presentation.dto.DecisaoOrcamento;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ActionTokenServiceTest {

  private ActionTokenService service;

  @BeforeEach
  void setUp() {
    service = new ActionTokenService();
    ReflectionTestUtils.setField(service, "secret", "test-secret-32-chars-minimum-ok!");
    ReflectionTestUtils.setField(service, "expiryMinutes", 1440);
  }

  @Test
  @DisplayName("Token gerado deve ser validado com sucesso")
  void deveGerarEValidarTokenComSucesso() {
    UUID orcamentoId = UUID.randomUUID();

    String token = service.generate(orcamentoId, DecisaoOrcamento.APROVADO);
    var payload = service.validate(token);

    assertThat(payload).isPresent();
    assertThat(payload.get().orcamentoId()).isEqualTo(orcamentoId);
    assertThat(payload.get().decisao()).isEqualTo(DecisaoOrcamento.APROVADO);
  }

  @Test
  @DisplayName("Token de reprovação deve ser validado com decisao REPROVADO")
  void deveGerarTokenDeReprovacaoComDecisaoCorreta() {
    UUID orcamentoId = UUID.randomUUID();

    String token = service.generate(orcamentoId, DecisaoOrcamento.REPROVADO);
    var payload = service.validate(token);

    assertThat(payload).isPresent();
    assertThat(payload.get().decisao()).isEqualTo(DecisaoOrcamento.REPROVADO);
  }

  @Test
  @DisplayName("Tokens gerados para o mesmo orçamento com decisoes distintas devem ser diferentes")
  void deveGerarTokensDistintosPorDecisao() {
    UUID orcamentoId = UUID.randomUUID();

    String aprovarToken = service.generate(orcamentoId, DecisaoOrcamento.APROVADO);
    String reprovarToken = service.generate(orcamentoId, DecisaoOrcamento.REPROVADO);

    assertThat(aprovarToken).isNotEqualTo(reprovarToken);
  }

  @Test
  @DisplayName("Token com assinatura adulterada deve ser rejeitado")
  void deveRejeitarTokenComAssinaturaAdulterada() {
    UUID orcamentoId = UUID.randomUUID();
    String token = service.generate(orcamentoId, DecisaoOrcamento.APROVADO);

    String tampered = token.substring(0, token.length() - 4) + "XXXX";
    var payload = service.validate(tampered);

    assertThat(payload).isEmpty();
  }

  @Test
  @DisplayName("Token sem ponto separador deve ser rejeitado")
  void deveRejeitarTokenSemPonto() {
    var payload = service.validate("tokenSemPonto");
    assertThat(payload).isEmpty();
  }

  @Test
  @DisplayName("Token nulo deve ser rejeitado")
  void deveRejeitarTokenNulo() {
    var payload = service.validate(null);
    assertThat(payload).isEmpty();
  }

  @Test
  @DisplayName("Token expirado deve ser rejeitado")
  void deveRejeitarTokenExpirado() {
    ReflectionTestUtils.setField(service, "expiryMinutes", -1);

    UUID orcamentoId = UUID.randomUUID();
    String token = service.generate(orcamentoId, DecisaoOrcamento.APROVADO);
    var payload = service.validate(token);

    assertThat(payload).isEmpty();
  }

  @Test
  @DisplayName("Token gerado com secret diferente deve ser rejeitado")
  void deveRejeitarTokenGeradoComSecretDiferente() {
    UUID orcamentoId = UUID.randomUUID();
    String token = service.generate(orcamentoId, DecisaoOrcamento.APROVADO);

    ActionTokenService otherService = new ActionTokenService();
    ReflectionTestUtils.setField(otherService, "secret", "outro-secret-completamente-diferente!");
    ReflectionTestUtils.setField(otherService, "expiryMinutes", 1440);

    var payload = otherService.validate(token);
    assertThat(payload).isEmpty();
  }
}
