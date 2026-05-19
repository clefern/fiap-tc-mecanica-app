package com.fiap.mecanica.infra.monitoring;

import com.fiap.mecanica.application.email.EmailSender;
import com.fiap.mecanica.application.service.PdfService;
import com.fiap.mecanica.domain.repository.BaseRepository;
import com.fiap.mecanica.domain.service.NotificationService;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.fiap.mecanica.infra.monitoring.MonitoredOperationType.*;
import static com.fiap.mecanica.infra.monitoring.Tags.*;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MonitoredOperationAspect {

	private final MeterRegistry meter;

	@Around(
		"@within(com.fiap.mecanica.infra.monitoring.MonitoredOperation) || " +
		"@within(com.fiap.mecanica.infra.monitoring.MonitoredOperations) || " +
		"@annotation(com.fiap.mecanica.infra.monitoring.MonitoredOperation) || " +
		"@annotation(com.fiap.mecanica.infra.monitoring.MonitoredOperations)"
	)
	public Object observability(final ProceedingJoinPoint pjp) throws Throwable {
		final var startNanos = System.nanoTime();
		final var annotations = getMonitoredOperations(pjp);

		var status = "success";
		var errorType = "";

		try {
			return pjp.proceed();
		} catch (final Throwable e) {
			status = "error";
			errorType = e.getClass().getSimpleName();
			throw e;
		} finally {
			final var elapsed = System.nanoTime() - startNanos;
			final var serviceName = pjp.getTarget().getClass().getSimpleName();
			final var operation = pjp.getSignature().getName();

			final var tags = List.<Tag>of(
				new ImmutableTag(STATUS_TAG, status),
				new ImmutableTag(OPERATION_TAG, operation),
				new ImmutableTag(SERVICE_TAG, serviceName),
				new ImmutableTag(ERROR_TYPE, errorType)
			);

			if ("error".equals(status)) {
				log.error("[OBSERVABILITY-ERROR] Svc: {} | Op: {} | Error: {}", serviceName, operation, errorType);
				meter.counter(errorType, tags).increment();
				meter.counter(APPLICATION_ERROR.value(), tags).increment();

				final var target = pjp.getTarget();
				if (target instanceof BaseRepository ||
					target instanceof EmailSender ||
					target instanceof NotificationService ||
					target instanceof PdfService) {
					meter.counter(APPLICATION_INTEGRATION_ERROR.value(), tags).increment();
				}
			}

			for (final var annotation : annotations) {
				final var name = annotation.type().value();
				log.debug("[OBSERVABILITY] Metric: {} | Status: {} | Svc: {}", name, status, serviceName);

				switch (annotation.type()) {
					case MECHANIC_PERFORMANCE,
					     EMAIL_SEND_OS_QUOTE,
					     EMAIL_SEND_OS_CONFIRMATION,
					     EMAIL_DELIVERY_RATE,
					     INVENTORY_ADDED,
					     INVENTORY_REMOVED,
					     INVENTORY_UPDATED,
					     INVENTORY_TURNOVER,
					     OS_DIAGNOSIS_STARTED,
					     OS_DIAGNOSIS_COMPLETED,
					     OS_MECHANIC_CHANGED,
					     OS_CREATED,
					     OS_STARTED,
					     OS_COMPLETED,
					     OS_DELIVERED,
					     OS_APPROVED,
					     OS_CANCELLED,
					     OS_CONVERSION_RATE,
					     BUDGET_CREATED,
					     BUDGET_APPROVED,
					     BUDGET_REPROVED,
					     BUDGET_CANCELLED,
					     REPORT_MECHANIC -> meter.counter(name, tags).increment();

					case OS_CYCLE_TIME,
					     APPLICATION_PERFORMANCE_DB_QUERY,
					     REPORT_OS_AVERAGE_EXECUTION_TIME,
					     APPLICATION_LATENCY_API,
					     REPORT_PDF_GENERATION_TIME -> Timer.builder(name)
						.tags(tags)
						.publishPercentileHistogram()
						.register(meter)
						.record(elapsed, TimeUnit.NANOSECONDS);

					case TEST_A, TEST_B, APPLICATION_ERROR, APPLICATION_INTEGRATION_ERROR -> { /* IGNORED */ }
				}
			}
		}
	}

	private MonitoredOperation[] getMonitoredOperations(final ProceedingJoinPoint pjp) {
		final var method = ((MethodSignature) pjp.getSignature()).getMethod();
		final var targetClass = pjp.getTarget().getClass();
		final var map = new HashMap<MonitoredOperationType, MonitoredOperation>();

		Stream.concat(
			AnnotatedElementUtils.getMergedRepeatableAnnotations(method, MonitoredOperation.class).stream(),
			AnnotatedElementUtils.getMergedRepeatableAnnotations(targetClass, MonitoredOperation.class).stream()
		).forEach(ann -> map.putIfAbsent(ann.type(), ann));

		return map.values().toArray(new MonitoredOperation[0]);
	}
}
