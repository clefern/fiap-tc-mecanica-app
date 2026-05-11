package com.fiap.mecanica.infra.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.fiap.mecanica.infra.jpa")
@EntityScan(basePackages = "com.fiap.mecanica.infra.entity")
public class JpaConfig {
  // Additional JPA-specific configuration can be added here if needed
}
