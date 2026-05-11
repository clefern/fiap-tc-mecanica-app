package com.fiap.mecanica.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fiap.mecanica.application.service.impl.ServicoServiceImpl;
import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.domain.repository.ServicoRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class ServicoServiceTest {

  private ServicoService service;

  @Mock private ServicoRepository repository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new ServicoServiceImpl(repository);
  }

  @Test
  void deveCriarServico() {
    Servico servico =
        new Servico(
            "Nome", "Desc", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS);
    when(repository.save(any(Servico.class))).thenReturn(servico);

    Servico created = service.create(servico);

    assertNotNull(created);
    verify(repository, times(1)).save(servico);
  }

  @Test
  void deveAtualizarServicoExistente() {
    UUID id = UUID.randomUUID();
    Servico existing =
        new Servico(
            "Antigo", "Desc", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS);
    Servico updateInfo =
        new Servico(
            "Novo",
            "Desc Nova",
            BigDecimal.ONE,
            Duration.ofMinutes(60),
            CategoriaServico.MANUTENCAO_PREVENTIVA);

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.save(any(Servico.class))).thenReturn(existing);

    Servico updated = service.update(id, updateInfo);

    assertEquals("Novo", updated.getNome());
    assertEquals(CategoriaServico.MANUTENCAO_PREVENTIVA, updated.getCategoria());
    verify(repository, times(1)).save(existing);
  }

  @Test
  void deveLancarErroAoAtualizarInexistente() {
    UUID id = UUID.randomUUID();
    Servico updateInfo =
        new Servico(
            "Novo", "Desc", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS);

    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ServicoNaoEncontradoException.class, () -> service.update(id, updateInfo));
  }

  @Test
  void deveListarAtivos() {
    Pageable pageable = PageRequest.of(0, 10);
    when(repository.findByAtivoTrue(pageable))
        .thenReturn(
            new PageImpl<>(
                List.of(
                    new Servico(
                        "Nome",
                        "Desc",
                        BigDecimal.TEN,
                        Duration.ofMinutes(30),
                        CategoriaServico.OUTROS))));

    Page<Servico> ativos = service.getAllAtivos(pageable);

    assertFalse(ativos.isEmpty());
    verify(repository, times(1)).findByAtivoTrue(pageable);
  }

  @Test
  void deveBuscarPorId() {
    UUID id = UUID.randomUUID();
    Servico servico =
        new Servico(
            "Nome", "Desc", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS);
    when(repository.findById(id)).thenReturn(Optional.of(servico));

    Optional<Servico> found = service.getById(id);

    assertTrue(found.isPresent());
    assertEquals(servico, found.get());
    verify(repository, times(1)).findById(id);
  }

  @Test
  void deveListarTodos() {
    Pageable pageable = PageRequest.of(0, 10);
    when(repository.findAll(pageable))
        .thenReturn(
            new PageImpl<>(
                List.of(
                    new Servico(
                        "Nome",
                        "Desc",
                        BigDecimal.TEN,
                        Duration.ofMinutes(30),
                        CategoriaServico.OUTROS))));

    Page<Servico> all = service.getAll(pageable);

    assertFalse(all.isEmpty());
    verify(repository, times(1)).findAll(pageable);
  }

  @Test
  void deveExcluirServico() {
    UUID id = UUID.randomUUID();
    doNothing().when(repository).delete(id);

    service.delete(id);

    verify(repository, times(1)).delete(id);
  }
}
