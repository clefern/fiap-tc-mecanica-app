package com.fiap.mecanica.infra.seeding.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SeederFactoryTest {

  private static class TestEntity {
    String value;

    public TestEntity(String value) {
      this.value = value;
    }
  }

  private static class TestFactory extends SeederFactory<TestEntity> {
    @Override
    public TestEntity create() {
      return new TestEntity("test");
    }
  }

  @Test
  @DisplayName("Should create many entities")
  void shouldCreateManyEntities() {
    TestFactory factory = new TestFactory();
    List<TestEntity> entities = factory.createMany(5);

    assertThat(entities).hasSize(5);
    assertThat(entities).allMatch(e -> "test".equals(e.value));
  }
}
