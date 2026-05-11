package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.exception.DuplicateDocumentoException;
import com.fiap.mecanica.domain.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.repository.MecanicoRepository;
import com.fiap.mecanica.domain.service.NotificationService;
import com.fiap.mecanica.domain.service.PasswordPolicy;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MecanicoServiceImplTest {

  @Mock private MecanicoRepository repository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private NotificationService notificationService;
  @Mock private PasswordPolicy passwordPolicy;

  @InjectMocks private MecanicoServiceImpl service;

  @Test
  @DisplayName("Deve criar mecânico com sucesso")
  void shouldCreateMecanico() {
    Mecanico m =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Mecânica de Motor");
    when(repository.existsByCpf(any(CPF.class))).thenReturn(false);
    when(repository.save(any(Mecanico.class))).thenAnswer(i -> i.getArgument(0));
    when(passwordPolicy.generateRandomPassword()).thenReturn("randomPass");
    when(passwordEncoder.encode("randomPass")).thenReturn("encodedPass");

    Mecanico created = service.create(m);

    assertThat(created).isNotNull();
    assertThat(created.getPassword()).isEqualTo("encodedPass");
    verify(repository).save(m);

    ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
    verify(notificationService).sendWelcomeEmail(eq(m), passwordCaptor.capture());
    verify(passwordEncoder).encode(passwordCaptor.getValue());
  }

  @Test
  @DisplayName("Não deve criar mecânico com CPF duplicado")
  void shouldThrowWhenCpfExists() {
    Mecanico m =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Mecânica de Motor");
    when(repository.existsByCpf(any(CPF.class))).thenReturn(true);

    assertThrows(DuplicateDocumentoException.class, () -> service.create(m));
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldGetById() {
    UUID id = UUID.randomUUID();
    Mecanico m =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Mecânica de Motor");
    when(repository.findById(id)).thenReturn(Optional.of(m));

    Optional<Mecanico> result = service.getById(id);
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(m);
  }

  @Test
  @DisplayName("Deve buscar por CPF")
  void shouldGetByCpf() {
    String cpf = "52998224725";
    Mecanico m = new Mecanico("João", CPF.of(cpf), Email.of("j@o.com"), "Mecânica de Motor");
    when(repository.findByCpf(any(CPF.class))).thenReturn(Optional.of(m));

    Optional<Mecanico> result = service.getByCpf(cpf);
    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Deve atualizar mecânico")
  void shouldUpdate() {
    UUID id = UUID.randomUUID();
    Mecanico existing =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Mecânica de Motor");
    existing.setId(id);

    Mecanico updateData =
        new Mecanico(
            "João Silva", CPF.of("529.982.247-25"), Email.of("novo@o.com"), "Freios e ABS");

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.findByCpf(any(CPF.class))).thenReturn(Optional.of(existing));
    when(repository.save(any(Mecanico.class))).thenAnswer(i -> i.getArgument(0));

    Mecanico updated = service.update(id, updateData);

    assertThat(updated.getNome()).isEqualTo("João Silva");
    assertThat(updated.getEspecialidade()).isEqualTo("Freios e ABS");
    assertThat(updated.getEmail().value()).isEqualTo("novo@o.com");
  }

  @Test
  @DisplayName("Deve atualizar mecânico quando CPF é alterado para um novo (único)")
  void shouldUpdateWhenCpfChangedAndUnique() {
    UUID id = UUID.randomUUID();
    Mecanico existing =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Mecânica de Motor");
    existing.setId(id);

    Mecanico updateData =
        new Mecanico(
            "João Silva", CPF.of("390.533.447-05"), Email.of("novo@o.com"), "Freios e ABS");

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.findByCpf(any(CPF.class))).thenReturn(Optional.empty());
    when(repository.save(any(Mecanico.class))).thenAnswer(i -> i.getArgument(0));

    Mecanico updated = service.update(id, updateData);

    assertThat(updated.getNome()).isEqualTo("João Silva");
    assertThat(updated.getCpf().valor()).isEqualTo("39053344705");
  }

  @Test
  @DisplayName("Não deve atualizar se CPF já existe em outro mecânico")
  void shouldThrowWhenUpdateCpfExistsForOther() {
    UUID id = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();
    Mecanico updateData =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Motor");
    Mecanico existing =
        new Mecanico("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"), "Freios");
    existing.setId(otherId);

    // Use a mock CPF for the current mechanic to avoid validation logic
    CPF mockCpf = mock(CPF.class);
    when(repository.findById(id))
        .thenReturn(Optional.of(new Mecanico("João", mockCpf, Email.of("j@o.com"), "Motor")));
    when(repository.findByCpf(any(CPF.class))).thenReturn(Optional.of(existing));

    assertThrows(DuplicateDocumentoException.class, () -> service.update(id, updateData));
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Deve deletar mecânico")
  void shouldDelete() {
    UUID id = UUID.randomUUID();
    doNothing().when(repository).deleteById(id);

    service.delete(id);

    verify(repository).deleteById(id);
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar mecânico não encontrado")
  void shouldThrowWhenUpdateNotFound() {
    UUID id = UUID.randomUUID();
    Mecanico updateData =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Motor");

    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(MecanicoNaoEncontradoException.class, () -> service.update(id, updateData));
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar por documento inválido (CNPJ)")
  void shouldThrowWhenGetByCpfIsCnpj() {
    // CNPJ valid format but invalid for Mecanico lookup
    String cnpj = "12.345.678/0001-95";
    assertThrows(IllegalArgumentException.class, () -> service.getByCpf(cnpj));
  }

  @Test
  @DisplayName("Deve listar todos os mecânicos")
  void shouldGetAll() {
    Pageable pageable = PageRequest.of(0, 10);
    when(repository.findAll(pageable))
        .thenReturn(new PageImpl<>(List.of(mock(Mecanico.class), mock(Mecanico.class))));

    Page<Mecanico> result = service.getAll(pageable);

    assertThat(result).hasSize(2);
    verify(repository).findAll(pageable);
  }
}
