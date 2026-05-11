package com.fiap.mecanica.domain.service;

import java.security.SecureRandom;

public class PasswordGenerator {

  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
  private static final int DEFAULT_LENGTH = 8;
  private static final SecureRandom RANDOM = new SecureRandom();

  public static String generate() {
    return generate(DEFAULT_LENGTH);
  }

  public static String generate(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
    }
    return sb.toString();
  }
}
