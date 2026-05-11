package com.fiap.mecanica.infra.entity;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orcamentos", schema = "public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String codigo;

  @Column(name = "ordem_servico_id", nullable = false)
  private UUID ordemServicoId;

  @Column(name = "mecanico_diagnostico_id")
  private UUID mecanicoDiagnosticoId;

  @Column(name = "data_emissao", nullable = false)
  private LocalDateTime dataEmissao;

  @Column(name = "data_validade", nullable = false)
  private LocalDateTime dataValidade;

  @Column(name = "valor_total_materiais", precision = 19, scale = 2)
  private BigDecimal valorTotalMateriais;

  @Column(name = "valor_total_mao_de_obra", precision = 19, scale = 2)
  private BigDecimal valorTotalMaoDeObra;

  @Column(name = "valor_impostos", precision = 19, scale = 2)
  private BigDecimal valorImpostos;

  @Column(name = "valor_total", nullable = false, precision = 19, scale = 2)
  private BigDecimal valorTotal;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusOrcamento status;

  @Column(name = "url_pdf")
  private String urlPdf;
}
