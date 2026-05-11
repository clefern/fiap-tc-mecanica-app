package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.presentation.validation.EmailNormalized;
import com.fiap.mecanica.presentation.validation.Telefone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload para criação/atualização de Cliente")
public class ClienteRequest {
  @Schema(example = "João da Silva")
  @NotBlank
  private String nome;

  @Schema(example = "39053344705", description = "CPF ou CNPJ (apenas números)")
  @NotBlank
  private String documento;

  @Schema(example = "FISICA", description = "Tipo de Pessoa (FISICA ou JURIDICA)")
  @NotNull private TipoPessoa tipoPessoa;

  @Schema(example = "joao@example.com")
  @NotBlank
  @EmailNormalized
  private String email;

  @Schema(example = "11987654321", description = "Telefone BR sem formatação")
  @NotBlank
  @Telefone
  private String telefone;

  @Schema(example = "Rua A, 123")
  @NotBlank
  private String endereco;
}
