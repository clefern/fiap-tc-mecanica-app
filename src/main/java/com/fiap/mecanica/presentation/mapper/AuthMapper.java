package com.fiap.mecanica.presentation.mapper;

import com.fiap.mecanica.domain.model.command.ResetPasswordCommand;
import com.fiap.mecanica.presentation.dto.auth.ForgotPasswordRequest;
import com.fiap.mecanica.presentation.dto.auth.ResetPasswordRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

  public String toDomain(ForgotPasswordRequest request) {
    if (request == null) {
      return null;
    }
    return request.getEmail();
  }

  public ResetPasswordCommand toDomain(ResetPasswordRequest request) {
    if (request == null) {
      return null;
    }
    return ResetPasswordCommand.builder()
        .token(request.getToken())
        .newPassword(request.getNewPassword())
        .build();
  }
}
