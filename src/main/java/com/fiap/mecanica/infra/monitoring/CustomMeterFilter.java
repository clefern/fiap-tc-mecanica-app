package com.fiap.mecanica.infra.monitoring;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuração centralizada para métricas do Micrometer/Actuator.
 */
@Component
@RequiredArgsConstructor
public class CustomMeterFilter implements MeterFilter {

	@Override
	public DistributionStatisticConfig configure(
		final Meter.Id id,
		final DistributionStatisticConfig config
	) {
		if (id.getType() != Meter.Type.TIMER) {
			return config;
		}

		return DistributionStatisticConfig.builder()
			.percentilesHistogram(true)
			.expiry(Duration.ofMinutes(2))
			.bufferLength(5)
			.build()
			.merge(config);
	}
}
