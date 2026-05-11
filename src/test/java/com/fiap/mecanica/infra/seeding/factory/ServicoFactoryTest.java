package com.fiap.mecanica.infra.seeding.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.model.Servico;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServicoFactoryTest {

  private final ServicoFactory factory = new ServicoFactory();

  @Test
  @DisplayName("Should create single service with valid data")
  void shouldCreateSingleService() {
    Servico servico = factory.create();

    assertThat(servico).isNotNull();
    assertThat(servico.getNome()).isNotBlank();
    assertThat(servico.getDescricao()).isNotBlank();
    assertThat(servico.getPrecoBase()).isNotNull();
    assertThat(servico.getPrecoBase()).isGreaterThanOrEqualTo(new java.math.BigDecimal("0.01"));
    assertThat(servico.getTempoEstimado()).isNotNull();
    assertThat(servico.getTempoEstimado().isZero()).isFalse();
    assertThat(servico.getTempoEstimado().isNegative()).isFalse();
    assertThat(servico.getCategoria()).isNotNull();
  }

  @Test
  @DisplayName("Should create multiple services with unique names in a batch")
  void shouldCreateMultipleServicesWithUniqueNames() {
    int count = 10;
    List<Servico> servicos = factory.createMany(count);

    assertThat(servicos).hasSize(count);
    Set<String> uniqueNames = servicos.stream().map(Servico::getNome).collect(Collectors.toSet());
    assertThat(uniqueNames).hasSize(count);
  }

  @Test
  @DisplayName("Should enforce minimum duration when template has zero time range")
  void shouldEnforceMinimumDurationForZeroTimeTemplate() throws Exception {
    Field queueField = ServicoFactory.class.getDeclaredField("templateQueue");
    queueField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Queue<Object> queue = (Queue<Object>) queueField.get(factory);
    queue.clear();

    Class<?> templateClass = null;
    for (Class<?> inner : ServicoFactory.class.getDeclaredClasses()) {
      if ("ServicoTemplate".equals(inner.getSimpleName())) {
        templateClass = inner;
        break;
      }
    }

    assertThat(templateClass).isNotNull();

    Constructor<?> constructor = templateClass.getDeclaredConstructors()[0];
    constructor.setAccessible(true);
    Object template =
        constructor.newInstance(
            "Servico Zero Time", "Descricao", 100.0, 200.0, 0, 0, CategoriaServico.OUTROS);

    queue.add(template);

    Servico servico = factory.create();

    assertThat(servico.getTempoEstimado()).isNotNull();
    assertThat(servico.getTempoEstimado().toMinutes()).isGreaterThanOrEqualTo(30);
  }
}
