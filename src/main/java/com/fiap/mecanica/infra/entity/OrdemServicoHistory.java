package com.fiap.mecanica.infra.entity;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.OrdemServico;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.fiap.mecanica.infra.entity.OrdemServicoHistory.TABLE;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TABLE)
public class OrdemServicoHistory implements Serializable {
	public static final String TABLE = "os_history";

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@Column(name = "ordem_servico_id", nullable = false)
	private UUID ordemServicoId;
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private StatusOS status;
	@Column(name = "started_at", nullable = false)
	private LocalDateTime startedAt;
	@Column(name = "ended_at")
	private LocalDateTime endedAt;

	public static OrdemServicoHistory create(final OrdemServico os) {
		return OrdemServicoHistory.builder()
			.ordemServicoId(os.getId())
			.status(os.getStatus())
			.startedAt(LocalDateTime.now())
			.build();
	}
}
