package com.fiap.mecanica.domain.service.impl;

import com.fiap.mecanica.domain.service.PasswordGenerator;
import com.fiap.mecanica.domain.service.PasswordPolicy;
import org.springframework.stereotype.Service;

@Service
public class DefaultPasswordPolicy implements PasswordPolicy {

  @Override
  public String generateRandomPassword() {
    return PasswordGenerator.generate();
  }
}
