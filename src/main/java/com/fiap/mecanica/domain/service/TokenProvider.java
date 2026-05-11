package com.fiap.mecanica.domain.service;

import com.fiap.mecanica.domain.model.TokenPair;
import com.fiap.mecanica.domain.model.User;

public interface TokenProvider {
  TokenPair generateTokenPair(User user);
}
