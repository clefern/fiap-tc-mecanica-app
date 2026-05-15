package com.fiap.mecanica.application.listeners;

import com.fiap.mecanica.application.events.*;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoHistoryRepository;
import com.fiap.mecanica.infra.entity.OrdemServicoHistory;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.fiap.mecanica.infra.monitoring.Tags.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OsHistoryListener {

	private final OrdemServicoHistoryRepository historyRepository;
	private final MeterRegistry meterRegistry;

	@EventListener
	@Transactional
	public void handleOsCriada(OsCriadaEvent event) {
		registrarMudancaStatus(event.getOrdemServico());
	}

	@EventListener
	@Transactional
	public void handleOsEmDiagnostico(OrdemServicoEmDiagnosticoEvent event) {
		registrarMudancaStatus(event.getOrdemServico());
	}

	@EventListener
	@Transactional
	public void handleOsAguardandoAprovacao(OrdemServicoAguardandoAprovacaoEvent event) {
		registrarMudancaStatus(event.getOrdemServico());
	}

	@EventListener
	@Transactional
	public void handleOsAprovada(OrdemServicoAprovadaEvent event) {
		registrarMudancaStatus(event.getOrdemServico());
	}

	@EventListener
	@Transactional
	public void handleOsEmExecucao(OrdemServicoEmExecucaoEvent event) {
		registrarMudancaStatus(event.getOrdemServico());
	}

	@EventListener
	@Transactional
	public void handleOsFinalizada(OsFinalizadaEvent event) {
		registrarMudancaStatus(event.getOrdemServico());
	}

	@EventListener
	@Transactional
	public void handleOsEntregue(OrdemServicoEntregueEvent event) {
		registrarMudancaStatus(event.getOrdemServico());
	}

	@EventListener
	@Transactional
	public void handleOsCancelada(OrdemServicoCanceladaEvent event) {
		registrarMudancaStatus(event.getOrdemServico());
	}

	private void registrarMudancaStatus(OrdemServico os) {
		log.debug("[OS_TRACKER] Registrando status {} para OS {}", os.getStatus(), os.getId());

		final var agora = LocalDateTime.now();

		// 1. Finaliza o status anterior se existir e registra métrica de duração
		historyRepository.findLatestByOrdemServicoId(os.getId()).ifPresent(history -> {
			if (history.getStatus() != os.getStatus()) {
				history.setEndedAt(agora);
				historyRepository.save(history);

				// Registro de métrica de negócio para o New Relic via OTel
				registrarMetricaCiclo(history, agora);
			}
		});

		// 2. Inicia o novo status (se for diferente do último registrado)
		historyRepository.findLatestByOrdemServicoId(os.getId()).ifPresentOrElse(
			last -> {
				if (last.getStatus() != os.getStatus()) {
					createNewHistory(os);
				}
			},
			() -> createNewHistory(os)
		);
	}

	private void registrarMetricaCiclo(final OrdemServicoHistory history, final LocalDateTime dataFim) {
		try {
			final var duration = Duration.between(history.getStartedAt(), dataFim);

			Timer.builder(MonitoredOperationType.OS_CYCLE_TIME.value())
				.description("Tempo de permanência da OS em cada status (Lead Time)")
				.tag(STATUS_TAG, history.getStatus().name())
				.tag(SERVICE_TAG, getClass().getSimpleName())
				.tag(METRIC_TYPE_TAG, "business")
				.register(meterRegistry)
				.record(duration);

			log.info("[METRICA_NEGOCIO] OS_ID={} Status={} Duracao={}",
				history.getOrdemServicoId(), history.getStatus(), duration);
		} catch (Exception e) {
			log.error("Erro ao registrar métrica de ciclo para OS: {}", history.getOrdemServicoId(), e);
		}
	}

	private void createNewHistory(OrdemServico os) {
		historyRepository.save(OrdemServicoHistory.create(os));
	}
}
