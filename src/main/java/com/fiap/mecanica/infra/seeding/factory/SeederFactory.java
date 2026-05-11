package com.fiap.mecanica.infra.seeding.factory;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import net.datafaker.Faker;

/**
 * Base abstract factory for creating test/seed data. Inspired by Laravel's Model Factories.
 *
 * @param <T> The entity type
 */
public abstract class SeederFactory<T> {

  protected final Faker faker;

  protected SeederFactory() {
    this.faker = new Faker(Locale.of("pt-BR"));
  }

  /** Create a new instance of the entity with random data. Does not persist to database. */
  public abstract T create();

  /** Create multiple instances of the entity. Does not persist to database. */
  public List<T> createMany(int count) {
    return IntStream.range(0, count).mapToObj(i -> create()).toList();
  }
}
