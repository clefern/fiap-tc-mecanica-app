package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.infra.entity.MecanicoEntity;
import com.fiap.mecanica.infra.jpa.JpaMecanicoRepository;
import com.fiap.mecanica.infra.mapper.MecanicoEntityMapper;
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

class JpaMecanicoRepositoryAdapterTest {

  @Mock private JpaMecanicoRepository jpaRepository;

  @Mock private MecanicoEntityMapper mapper;

  @InjectMocks private JpaMecanicoRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldFindById() {
    UUID id = UUID.randomUUID();
    MecanicoEntity entity = new MecanicoEntity();
    entity.setId(id);
    entity.setNome("Joao");
    entity.setCpf("150.391.884-04");
    entity.setEmail("j@o.com");
    entity.setEspecialidade("Motor");
    entity.setAtivo(true);

    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    Mecanico domain = new Mecanico("Joao", CPF.of("150.391.884-04"), Email.of("j@o.com"), "Motor");
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<Mecanico> result = adapter.findById(id);

    assertThat(result).isPresent();
    assertThat(result.get().getNome()).isEqualTo("Joao");
    assertThat(result.get().getEspecialidade()).isEqualTo("Motor");
  }

  @Test
  @DisplayName("Deve buscar por CPF")
  void shouldFindByCpf() {
    String cpfFormatted = "150.391.884-04";
    String cpfDigits = "15039188404";
    MecanicoEntity entity = new MecanicoEntity();
    entity.setId(UUID.randomUUID());
    entity.setCpf("15039188404");
    entity.setNome("Joao");
    entity.setCpf(cpfDigits);
    entity.setEmail("j@o.com");
    entity.setEspecialidade("Motor");
    entity.setAtivo(true);

    when(jpaRepository.findByCpf(cpfDigits)).thenReturn(Optional.of(entity));
    Mecanico domain = new Mecanico("Joao", CPF.of("150.391.884-04"), Email.of("j@o.com"), "Motor");
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<Mecanico> result = adapter.findByCpf(CPF.of(cpfFormatted));

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Deve listar todos")
  void shouldFindAll() {
    MecanicoEntity entity = new MecanicoEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Joao");
    entity.setCpf("150.391.884-04");
    entity.setEmail("j@o.com");
    entity.setEspecialidade("Motor");
    entity.setAtivo(true);

    Pageable pageable = Pageable.unpaged();
    Page<MecanicoEntity> pageEntity = new PageImpl<>(List.of(entity));

    when(jpaRepository.findAll(pageable)).thenReturn(pageEntity);

    Page<Mecanico> result = adapter.findAll(pageable);
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("Deve salvar mecanico")
  void shouldSave() {
    Mecanico mecanico =
        new Mecanico("Joao", CPF.of("150.391.884-04"), Email.of("j@o.com"), "Motor");
    mecanico.ativar();

    MecanicoEntity entity = new MecanicoEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Joao");
    entity.setAtivo(true);

    when(jpaRepository.save(any(MecanicoEntity.class))).thenReturn(entity);
    when(mapper.toEntity(mecanico)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(mecanico);

    Mecanico result = adapter.save(mecanico);

    assertThat(result).isNotNull();
    verify(jpaRepository, times(1)).save(any(MecanicoEntity.class));
  }

  @Test
  @DisplayName("Deve verificar se existe por CPF")
  void shouldReturnTrueIfExistsByCpf() {
    String cpfFormatted = "150.391.884-04";
    String cpfDigits = "15039188404";
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
