package com.fiap.mecanica.infra.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.rate-limit")
public class SecurityProperties {

  private int capacity;
  private int refillTokens;
  private int refillDurationMinutes;

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  public int getRefillTokens() {
    return refillTokens;
  }

  public void setRefillTokens(int refillTokens) {
    this.refillTokens = refillTokens;
  }

  public int getRefillDurationMinutes() {
    return refillDurationMinutes;
  }

  public void setRefillDurationMinutes(int refillDurationMinutes) {
    this.refillDurationMinutes = refillDurationMinutes;
  }
}
