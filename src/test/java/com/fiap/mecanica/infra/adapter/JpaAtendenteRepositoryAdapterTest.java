package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.infra.entity.AtendenteEntity;
import com.fiap.mecanica.infra.jpa.JpaAtendenteRepository;
import com.fiap.mecanica.infra.mapper.AtendenteEntityMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class JpaAtendenteRepositoryAdapterTest {

  @Mock private JpaAtendenteRepository jpaRepository;

  @Mock private AtendenteEntityMapper mapper;

  @InjectMocks private JpaAtendenteRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldFindById() {
    UUID id = UUID.randomUUID();
    AtendenteEntity entity = new AtendenteEntity();
    entity.setId(id);
    entity.setNome("Maria");
    entity.setCpf("161.542.893-32");
    entity.setEmail("m@o.com");
    entity.setAtivo(true);

    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    Atendente domain = new Atendente("Maria", CPF.of("161.542.893-32"), Email.of("m@o.com"));
    when(mapper.toDomain(any(AtendenteEntity.class))).thenReturn(domain);

    Optional<Atendente> result = adapter.findById(id);

    assertThat(result).isPresent();
    assertThat(result.get().getNome()).isEqualTo("Maria");
  }

  @Test
  @DisplayName("Deve buscar por CPF")
  void shouldFindByCpf() {
    String cpfFormatted = "161.542.893-32";
    String cpfDigits = "16154289332";
    AtendenteEntity entity = new AtendenteEntity();
    entity.setId(UUID.randomUUID());
    entity.setCpf("16154289332");
    entity.setNome("Maria");
    entity.setCpf(cpfDigits);
    entity.setEmail("m@o.com");
    entity.setAtivo(true);

    when(jpaRepository.findByCpf(cpfDigits)).thenReturn(Optional.of(entity));
    Atendente domain = new Atendente("Maria", CPF.of("161.542.893-32"), Email.of("m@o.com"));
    when(mapper.toDomain(any(AtendenteEntity.class))).thenReturn(domain);

    Optional<Atendente> result = adapter.findByCpf(CPF.of(cpfFormatted));

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Deve listar todos")
  void shouldFindAll() {
    AtendenteEntity entity = new AtendenteEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Maria");
    entity.setCpf("16154289332");
    entity.setEmail("m@o.com");
    entity.setAtivo(true);

    Pageable pageable = Pageable.unpaged();
    Page<AtendenteEntity> pageEntity = new PageImpl<>(List.of(entity));
    Atendente domain = new Atendente("Maria", CPF.of("161.542.893-32"), Email.of("m@o.com"));
    when(mapper.toDomain(entity)).thenReturn(domain);

    when(jpaRepository.findAll(pageable)).thenReturn(pageEntity);

    Page<Atendente> result = adapter.findAll(pageable);
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("Deve salvar atendente")
  void shouldSave() {
    Atendente atendente = new Atendente("Maria", CPF.of("161.542.893-32"), Email.of("m@o.com"));
    atendente.ativar();

    AtendenteEntity entity = new AtendenteEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Maria");
    entity.setAtivo(true);

    when(mapper.toEntity(any(Atendente.class))).thenReturn(entity);
    when(mapper.toDomain(any(AtendenteEntity.class))).thenReturn(atendente);
    when(jpaRepository.save(any(AtendenteEntity.class))).thenReturn(entity);

    Atendente result = adapter.save(atendente);

    assertThat(result).isNotNull();
    verify(jpaRepository, times(1)).save(any(AtendenteEntity.class));
  }

  @Test
  @DisplayName("Deve verificar se existe por CPF")
  void shouldReturnTrueIfExistsByCpf() {
    String cpfFormatted = "161.542.893-32";
    String cpfDigits = "16154289332";
    when(jpaRepository.existsByCpf(cpfDigits)).thenReturn(true);

    boolean exists = adapter.existsByCpf(CPF.of(cpfFormatted));

    assertThat(exists).isTrue();
    verify(jpaRepository).existsByCpf(cpfDigits);
  }

  @Test
  @DisplayName("Deve deletar por ID")
  void shouldDeleteById() {
    UUID id = UUID.randomUUID();
    adapter.deleteById(id);
    verify(jpaRepository).deleteById(id);
  }
}
