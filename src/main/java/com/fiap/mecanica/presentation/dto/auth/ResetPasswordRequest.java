package com.fiap.mecanica.presentation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
  @NotBlank(message = "O token é obrigatório")
  private String token;

  @NotBlank(message = "O email é obrigatório")
  @Email(message = "Formato de email inválido")
  private String email;

  @NotBlank(message = "A nova senha é obrigatória")
  @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
  private String newPassword;
}
