package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.exception.AtendenteNaoEncontradoException;
import com.fiap.mecanica.domain.exception.DuplicateDocumentoException;
import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.repository.AtendenteRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AtendenteServiceImplTest {

  @Mock private AtendenteRepository repository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private NotificationService notificationService;
  @Mock private PasswordPolicy passwordPolicy;

  @InjectMocks private AtendenteServiceImpl service;

  @Test
  @DisplayName("Deve criar atendente com sucesso")
  void shouldCreateAtendente() {
    Atendente a = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    when(repository.existsByCpf(any(CPF.class))).thenReturn(false);
    when(repository.save(any(Atendente.class))).thenAnswer(i -> i.getArgument(0));
    when(passwordPolicy.generateRandomPassword()).thenReturn("randomPass");
    when(passwordEncoder.encode("randomPass")).thenReturn("encodedPass");

    Atendente created = service.create(a);

    assertThat(created).isNotNull();
    assertThat(created.getPassword()).isEqualTo("encodedPass");
    verify(repository).save(a);

    org.mockito.ArgumentCaptor<String> passwordCaptor =
        org.mockito.ArgumentCaptor.forClass(String.class);
    verify(notificationService).sendWelcomeEmail(eq(a), passwordCaptor.capture());
    verify(passwordEncoder).encode(passwordCaptor.getValue());
  }

  @Test
  @DisplayName("Não deve criar atendente com CPF duplicado")
  void shouldThrowWhenCpfExists() {
    Atendente a = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    when(repository.existsByCpf(any(CPF.class))).thenReturn(true);

    assertThrows(DuplicateDocumentoException.class, () -> service.create(a));
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldGetById() {
    UUID id = UUID.randomUUID();
    Atendente a = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    when(repository.findById(id)).thenReturn(Optional.of(a));

    Optional<Atendente> result = service.getById(id);
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(a);
  }

  @Test
  @DisplayName("Deve buscar por CPF")
  void shouldGetByCpf() {
    String cpf = "52998224725";
    Atendente a = new Atendente("Maria", CPF.of(cpf), Email.of("m@o.com"));
    when(repository.findByCpf(any(CPF.class))).thenReturn(Optional.of(a));

    Optional<Atendente> result = service.getByCpf(cpf);
    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Deve atualizar atendente")
  void shouldUpdate() {
    UUID id = UUID.randomUUID();
    Atendente existing = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    existing.setId(id);

    Atendente updateData =
        new Atendente("Maria Silva", CPF.of("529.982.247-25"), Email.of("novo@o.com"));

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.findByCpf(any(CPF.class))).thenReturn(Optional.of(existing));
    when(repository.save(any(Atendente.class))).thenAnswer(i -> i.getArgument(0));

    Atendente updated = service.update(id, updateData);

    assertThat(updated.getNome()).isEqualTo("Maria Silva");
    assertThat(updated.getEmail().value()).isEqualTo("novo@o.com");
  }

  @Test
  @DisplayName("Deve atualizar atendente quando CPF é alterado para um novo (único)")
  void shouldUpdateWhenCpfChangedAndUnique() {
    UUID id = UUID.randomUUID();
    Atendente existing = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    existing.setId(id);

    Atendente updateData =
        new Atendente("Maria Silva", CPF.of("390.533.447-05"), Email.of("novo@o.com"));

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.findByCpf(any(CPF.class))).thenReturn(Optional.empty());
    when(repository.save(any(Atendente.class))).thenAnswer(i -> i.getArgument(0));

    Atendente updated = service.update(id, updateData);

    assertThat(updated.getNome()).isEqualTo("Maria Silva");
    assertThat(updated.getCpf().valor()).isEqualTo("39053344705");
  }

  @Test
  @DisplayName("Não deve atualizar se CPF já existe em outro atendente")
  void shouldThrowWhenUpdateWithDuplicateCpf() {
    UUID id = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();
    Atendente existing = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    existing.setId(id);

    Atendente other = new Atendente("Outra", CPF.of("390.533.447-05"), Email.of("x@o.com"));
    other.setId(otherId);

    Atendente updateData =
        new Atendente("Maria Silva", CPF.of("390.533.447-05"), Email.of("novo@o.com"));

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.findByCpf(any(CPF.class))).thenReturn(Optional.of(other));

    assertThrows(DuplicateDocumentoException.class, () -> service.update(id, updateData));
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar erro ao atualizar atendente inexistente")
  void shouldThrowWhenUpdateNonExistent() {
    UUID id = UUID.randomUUID();
    Atendente updateData = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));

    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(AtendenteNaoEncontradoException.class, () -> service.update(id, updateData));
  }

  @Test
  @DisplayName("Deve listar todos")
  void shouldListAll() {
    Atendente a = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    Pageable pageable = PageRequest.of(0, 10);
    when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(a)));

    Page<Atendente> list = service.getAll(pageable);
    assertThat(list).hasSize(1);
    assertThat(list.getContent().get(0)).isEqualTo(a);
  }

  @Test
  @DisplayName("Deve deletar")
  void shouldDelete() {
    UUID id = UUID.randomUUID();
    doNothing().when(repository).deleteById(id);

    service.delete(id);

    verify(repository).deleteById(id);
  }
}
