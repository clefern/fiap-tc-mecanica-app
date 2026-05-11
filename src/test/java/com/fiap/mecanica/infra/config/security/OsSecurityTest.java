package com.fiap.mecanica.infra.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.domain.valueobject.Email;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class OsSecurityTest {

  private OrdemServicoRepository repository;
  private OsSecurity osSecurity;

  @BeforeEach
  void setUp() {
    repository = mock(OrdemServicoRepository.class);
    osSecurity = new OsSecurity(repository);
  }

  @Test
  @DisplayName("Admin pode gerenciar qualquer OS")
  void shouldAllowAdminManageOs() {
    UUID osId = UUID.randomUUID();
    Authentication auth = createAuth(UserRole.ADMIN, UUID.randomUUID());

    assertThat(osSecurity.canManage(auth, osId)).isTrue();
  }

  @Test
  @DisplayName("Dono (Mecânico Diagnóstico) pode gerenciar OS")
  void shouldAllowOwnerDiagnosticManageOs() {
    UUID userId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    OrdemServico os = OrdemServico.builder().id(osId).mecanicoDiagnosticoId(userId).build();
    Authentication auth = createAuth(UserRole.MECANICO, userId);

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canManage(auth, osId)).isTrue();
  }

  @Test
  @DisplayName("Dono (Mecânico Execução) pode gerenciar OS")
  void shouldAllowOwnerExecutionManageOs() {
    UUID userId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    OrdemServico os = OrdemServico.builder().id(osId).mecanicoExecucaoId(userId).build();
    Authentication auth = createAuth(UserRole.MECANICO, userId);

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canManage(auth, osId)).isTrue();
  }

  @Test
  @DisplayName("Não dono nem Admin NÃO pode gerenciar OS")
  void shouldDenyNonAdminNonOwnerManageOs() {
    UUID userId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    OrdemServico os =
        OrdemServico.builder().id(osId).mecanicoDiagnosticoId(UUID.randomUUID()).build();
    Authentication auth = createAuth(UserRole.MECANICO, userId);

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canManage(auth, osId)).isFalse();
  }

  @Test
  @DisplayName("Gerenciar retorna false se OS não encontrada")
  void shouldDenyManageWhenOsNotFound() {
    UUID osId = UUID.randomUUID();
    Authentication auth = createAuth(UserRole.MECANICO, UUID.randomUUID());

    when(repository.findById(osId)).thenReturn(Optional.empty());

    assertThat(osSecurity.canManage(auth, osId)).isFalse();
  }

  @Test
  @DisplayName("Mecânico pode trabalhar em OS sem dono (Self-Assignment)")
  void shouldAllowMechanicOnUnassignedOs() {
    UUID osId = UUID.randomUUID();
    OrdemServico os = OrdemServico.builder().id(osId).status(StatusOS.RECEBIDA).build();
    Authentication auth = createAuth(UserRole.MECANICO, UUID.randomUUID());

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canWorkOn(auth, osId)).isTrue();
  }

  @Test
  @DisplayName("Mecânico pode trabalhar em OS sem dono na fase de execução (Self-Assignment)")
  void shouldAllowMechanicOnUnassignedOsInExecutionPhase() {
    UUID osId = UUID.randomUUID();
    OrdemServico os = OrdemServico.builder().id(osId).status(StatusOS.EM_EXECUCAO).build();
    Authentication auth = createAuth(UserRole.MECANICO, UUID.randomUUID());

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canWorkOn(auth, osId)).isTrue();
  }

  @Test
  @DisplayName("Mecânico NÃO pode trabalhar em OS de outro mecânico na fase de execução")
  void shouldDenyMechanicOnOtherMechanicOsInExecutionPhase() {
    UUID myId = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    OrdemServico os =
        OrdemServico.builder()
            .id(osId)
            .status(StatusOS.EM_EXECUCAO)
            .mecanicoExecucaoId(otherId)
            .build();
    Authentication auth = createAuth(UserRole.MECANICO, myId);

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canWorkOn(auth, osId)).isFalse();
  }

  @Test
  @DisplayName("Mecânico pode trabalhar em sua própria OS")
  void shouldAllowMechanicOnOwnOs() {
    UUID userId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    OrdemServico os =
        OrdemServico.builder()
            .id(osId)
            .status(StatusOS.EM_DIAGNOSTICO)
            .mecanicoDiagnosticoId(userId)
            .build();
    Authentication auth = createAuth(UserRole.MECANICO, userId);

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canWorkOn(auth, osId)).isTrue();
  }

  @Test
  @DisplayName("Mecânico NÃO pode trabalhar em OS de outro mecânico")
  void shouldDenyMechanicOnOtherMechanicOs() {
    UUID myId = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    OrdemServico os =
        OrdemServico.builder()
            .id(osId)
            .status(StatusOS.EM_DIAGNOSTICO)
            .mecanicoDiagnosticoId(otherId)
            .build();
    Authentication auth = createAuth(UserRole.MECANICO, myId);

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canWorkOn(auth, osId)).isFalse();
  }

  @Test
  @DisplayName("Mecânico NÃO pode trabalhar em OS Finalizada")
  void shouldDenyMechanicOnFinishedOs() {
    UUID userId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    OrdemServico os =
        OrdemServico.builder()
            .id(osId)
            .status(StatusOS.FINALIZADA)
            .mecanicoExecucaoId(userId)
            .build();
    Authentication auth = createAuth(UserRole.MECANICO, userId);

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canWorkOn(auth, osId)).isFalse();
  }

  @Test
  @DisplayName("Não mecânico NÃO pode trabalhar em OS")
  void shouldDenyNonMechanicWorkOnOs() {
    UUID osId = UUID.randomUUID();
    Authentication auth = createAuth(UserRole.CLIENTE, UUID.randomUUID());

    assertThat(osSecurity.canWorkOn(auth, osId)).isFalse();
  }

  @Test
  @DisplayName("Trabalhar retorna false se OS não encontrada")
  void shouldDenyWorkWhenOsNotFound() {
    UUID osId = UUID.randomUUID();
    Authentication auth = createAuth(UserRole.MECANICO, UUID.randomUUID());

    when(repository.findById(osId)).thenReturn(Optional.empty());

    assertThat(osSecurity.canWorkOn(auth, osId)).isFalse();
  }

  @Test
  @DisplayName("Mecânico pode trabalhar em fase de execução se for o dono")
  void shouldAllowMechanicOnExecutionPhase() {
    UUID userId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    OrdemServico os =
        OrdemServico.builder()
            .id(osId)
            .status(StatusOS.EM_EXECUCAO)
            .mecanicoExecucaoId(userId)
            .build();
    Authentication auth = createAuth(UserRole.MECANICO, userId);

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canWorkOn(auth, osId)).isTrue();
  }

  @Test
  @DisplayName("Cliente pode aprovar sua própria OS")
  void shouldAllowClientApproveOwnOs() {
    UUID userId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    OrdemServico os =
        OrdemServico.builder()
            .id(osId)
            .status(StatusOS.AGUARDANDO_APROVACAO)
            .clienteId(userId)
            .build();
    Authentication auth = createAuth(UserRole.CLIENTE, userId);

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canApprove(auth, osId)).isTrue();
  }

  @Test
  @DisplayName("Cliente NÃO pode aprovar OS de outro")
  void shouldDenyClientApproveOtherOs() {
    UUID myId = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    OrdemServico os =
        OrdemServico.builder()
            .id(osId)
            .status(StatusOS.AGUARDANDO_APROVACAO)
            .clienteId(otherId)
            .build();
    Authentication auth = createAuth(UserRole.CLIENTE, myId);

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThat(osSecurity.canApprove(auth, osId)).isFalse();
  }

  @Test
  @DisplayName("Não cliente NÃO pode aprovar OS")
  void shouldDenyNonClientApproveOs() {
    UUID osId = UUID.randomUUID();
    Authentication auth = createAuth(UserRole.MECANICO, UUID.randomUUID());

    assertThat(osSecurity.canApprove(auth, osId)).isFalse();
  }

  @Test
  @DisplayName("Aprovar retorna false se OS não encontrada")
  void shouldDenyApproveWhenOsNotFound() {
    UUID osId = UUID.randomUUID();
    Authentication auth = createAuth(UserRole.CLIENTE, UUID.randomUUID());

    when(repository.findById(osId)).thenReturn(Optional.empty());

    assertThat(osSecurity.canApprove(auth, osId)).isFalse();
  }

  @Test
  @DisplayName("Retorna false se usuário não for CustomUserDetails (defensivo)")
  void shouldReturnFalseWhenPrincipalIsNotCustomUserDetails() {
    UUID osId = UUID.randomUUID();
    OrdemServico os =
        OrdemServico.builder().id(osId).mecanicoDiagnosticoId(UUID.randomUUID()).build();

    // Auth com principal string, não CustomUserDetails
    Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass");

    when(repository.findById(osId)).thenReturn(Optional.of(os));

    // Testa isOwner (chamado por canManage)
    assertThat(osSecurity.canManage(auth, osId)).isFalse();
  }

  private Authentication createAuth(UserRole role, UUID userId) {
    User user = new TestUser();
    user.setId(userId);
    user.setRole(role);
    user.setAtivo(true);
    user.setEmail(Email.of("test@example.com"));

    CustomUserDetails userDetails = new CustomUserDetails(user);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  private static class TestUser extends User {}
}
