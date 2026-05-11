package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.domain.repository.ServicoRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ServicoServiceImplTest {

  @Mock private ServicoRepository repository;

  @InjectMocks private ServicoServiceImpl service;

  private Servico servico;

  @BeforeEach
  void setUp() {
    servico =
        new Servico(
            UUID.randomUUID(),
            "Troca de Óleo",
            "Troca completa",
            new BigDecimal("150.00"),
            Duration.ofHours(1),
            CategoriaServico.MANUTENCAO_PREVENTIVA);
  }

  @Test
  @DisplayName("Should create Servico successfully")
  void shouldCreateServicoSuccessfully() {
    when(repository.save(servico)).thenReturn(servico);

    Servico result = service.create(servico);

    assertThat(result).isEqualTo(servico);
  }

  @Test
  @DisplayName("Should update Servico successfully")
  void shouldUpdateServicoSuccessfully() {
    UUID id = servico.getId();
    Servico updatedInfo =
        new Servico(
            id,
            "Troca de Óleo Atualizada",
            "Nova descrição",
            new BigDecimal("200.00"),
            Duration.ofHours(2),
            CategoriaServico.REPARO_MECANICO);

    when(repository.findById(id)).thenReturn(Optional.of(servico));
    when(repository.save(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Servico result = service.update(id, updatedInfo);

    assertThat(result.getNome()).isEqualTo(updatedInfo.getNome());
    assertThat(result.getDescricao()).isEqualTo(updatedInfo.getDescricao());
    assertThat(result.getPrecoBase()).isEqualTo(updatedInfo.getPrecoBase());
    assertThat(result.getCategoria()).isEqualTo(updatedInfo.getCategoria());
  }

  @Test
  @DisplayName("Should throw exception when updating unknown Servico")
  void shouldThrowExceptionWhenUpdatingUnknownServico() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(id, servico))
        .isInstanceOf(ServicoNaoEncontradoException.class);
  }

  @Test
  @DisplayName("Should get Servico by ID")
  void shouldGetServicoById() {
    UUID id = servico.getId();
    when(repository.findById(id)).thenReturn(Optional.of(servico));

    Optional<Servico> result = service.getById(id);

    assertThat(result).isPresent().contains(servico);
  }

  @Test
  @DisplayName("Should get all Servicos")
  void shouldGetAllServicos() {
    Page<Servico> page = Page.empty();
    when(repository.findAll(any(Pageable.class))).thenReturn(page);

    Page<Servico> result = service.getAll(Pageable.unpaged());

    assertThat(result).isEqualTo(page);
  }

  @Test
  @DisplayName("Should get all active Servicos")
  void shouldGetAllActiveServicos() {
    Page<Servico> page = Page.empty();
    when(repository.findByAtivoTrue(any(Pageable.class))).thenReturn(page);

    Page<Servico> result = service.getAllAtivos(Pageable.unpaged());

    assertThat(result).isEqualTo(page);
  }

  @Test
  @DisplayName("Should delete Servico")
  void shouldDeleteServico() {
    UUID id = servico.getId();
    service.delete(id);
    verify(repository).delete(id);
  }
}
