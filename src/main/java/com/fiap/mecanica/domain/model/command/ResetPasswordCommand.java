package com.fiap.mecanica.domain.model.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordCommand {
  private String token;
  private String newPassword;
}
