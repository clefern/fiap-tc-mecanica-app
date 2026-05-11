package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
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
public class ClienteResponse {
  private UUID id;

  @Schema(example = "João da Silva")
  private String nome;

  @Schema(example = "39053344705")
  private String documento;

  @Schema(example = "FISICA")
  private TipoPessoa tipoPessoa;

  @Schema(example = "joao@example.com")
  private String email;

  @Schema(example = "11987654321")
  private String telefone;

  @Schema(example = "Rua A, 123")
  private String endereco;
}
