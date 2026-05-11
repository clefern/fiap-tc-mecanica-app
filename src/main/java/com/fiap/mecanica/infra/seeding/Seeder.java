package com.fiap.mecanica.infra.seeding;

/** Contract for all seeding components. Each implementation is responsible for one domain area. */
public interface Seeder {
  void seed();
}
