package com.fiap.mecanica.infra.entity;

import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.enums.StatusOS;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "ordens_servico", schema = "public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "cliente_id", nullable = false)
  private UUID clienteId;

  @Column(name = "veiculo_id", nullable = false)
  private UUID veiculoId;

  @Column(name = "mecanico_execucao_id")
  private UUID mecanicoExecucaoId;

  @Column(name = "mecanico_diagnostico_id")
  private UUID mecanicoDiagnosticoId;

  @Column(name = "codigo", nullable = false, unique = true)
  private String codigo;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private StatusOS status;

  @Column(name = "valor_total", nullable = false)
  private BigDecimal valorTotal;

  @Column(name = "data_entrada", nullable = false)
  private LocalDateTime dataEntrada;

  @Column(name = "data_previsao")
  private LocalDateTime dataPrevisao;

  @Column(name = "data_fechamento")
  private LocalDateTime dataFechamento;

  @Column(name = "observacoes", columnDefinition = "TEXT")
  private String observacoes;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = "prioridade", nullable = false, columnDefinition = "INTEGER")
  private Prioridade prioridade;

  @Column(name = "data_aprovacao")
  private LocalDateTime dataAprovacao;

  @OneToMany(
      mappedBy = "ordemServico",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @BatchSize(size = 20)
  @Builder.Default
  private List<ItemOrdemServicoEntity> itens = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
