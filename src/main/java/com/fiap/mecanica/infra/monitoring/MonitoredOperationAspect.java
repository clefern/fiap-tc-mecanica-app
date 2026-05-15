package com.fiap.mecanica.infra.monitoring;

import com.fiap.mecanica.application.email.EmailSender;
import com.fiap.mecanica.application.service.PdfService;
import com.fiap.mecanica.domain.repository.BaseRepository;
import com.fiap.mecanica.domain.service.NotificationService;
import io.micrometer.core.instrument.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.fiap.mecanica.infra.monitoring.MonitoredOperationType.APPLICATION_ERROR;
import static com.fiap.mecanica.infra.monitoring.MonitoredOperationType.APPLICATION_INTEGRATION_ERROR;
import static com.fiap.mecanica.infra.monitoring.Tags.*;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@SuppressWarnings({ "java:S1192" })
public class MonitoredOperationAspect {

	private final MeterRegistry meter;

	@Around(
		"@within(com.fiap.mecanica.infra.monitoring.MonitoredOperation) || " +
		"@within(com.fiap.mecanica.infra.monitoring.MonitoredOperations) || " +
		"@annotation(com.fiap.mecanica.infra.monitoring.MonitoredOperation) || " +
		"@annotation(com.fiap.mecanica.infra.monitoring.MonitoredOperations)"
	)
	public Object observability(final ProceedingJoinPoint pjp) throws Throwable {
		final var annotations = resolveAnnotations(pjp);
		if (annotations.length == 0) {
			return pjp.proceed();
		}

		boolean setService = false;
		boolean setOperation = false;

		var serviceName = MDC.get(SERVICE_TAG);
		if (serviceName == null) {
			serviceName = ClassUtils.getUserClass(pjp.getTarget()).getSimpleName();
			MDC.put(SERVICE_TAG, serviceName);
			setService = true;
		}

		var operation = MDC.get(OPERATION_TAG);
		if (operation == null) {
			operation = pjp.getSignature().getName();
			MDC.put(OPERATION_TAG, operation);
			setOperation = true;
		}

		var status = "success";
		var errorType = "";
		final var startNanos = System.nanoTime();
		try {
			return pjp.proceed();
		} catch (final Throwable e) {
			status = "error";
			errorType = e.getClass().getSimpleName();
			throw e;
		} finally {
			final var elapsed = System.nanoTime() - startNanos;

			final var tags = List.<Tag>of(
				new ImmutableTag(STATUS_TAG, status),
				new ImmutableTag(OPERATION_TAG, operation),
				new ImmutableTag(SERVICE_TAG, serviceName),
				new ImmutableTag(ERROR_TYPE, errorType)
			);

			if ("error".equals(status)) {
				meter.counter(errorType, tags).increment();
				meter.counter(APPLICATION_ERROR.value(), tags).increment();

				switch (pjp.getTarget()) {
					case BaseRepository o -> meter.counter(APPLICATION_INTEGRATION_ERROR.value(), tags).increment();
					case EmailSender o -> meter.counter(APPLICATION_INTEGRATION_ERROR.value(), tags).increment();
					case NotificationService o -> meter.counter(APPLICATION_INTEGRATION_ERROR.value(), tags).increment();
					case PdfService o -> meter.counter(APPLICATION_INTEGRATION_ERROR.value(), tags).increment();
					case null, default -> { /* ignored */ }
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
					     REPORT_MECHANIC -> Counter.builder(name)
						.tags(tags)
						.register(meter)
						.increment();

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

			if (setService) MDC.remove(SERVICE_TAG);
			if (setOperation) MDC.remove(OPERATION_TAG);
		}
	}

	private MonitoredOperation[] resolveAnnotations(final ProceedingJoinPoint pjp) {
		final var signature = (MethodSignature) pjp.getSignature();
		var method = signature.getMethod();
		final var target = pjp.getTarget().getClass();

		if (target != method.getDeclaringClass()) {
			method = ClassUtils.getMostSpecificMethod(method, target);
		}

		final var map = new LinkedHashMap<MonitoredOperationType, MonitoredOperation>();
		Stream.concat(
			AnnotatedElementUtils.getMergedRepeatableAnnotations(method, MonitoredOperation.class).stream(),
			AnnotatedElementUtils.getMergedRepeatableAnnotations(target, MonitoredOperation.class).stream()
		).forEach(ann -> map.putIfAbsent(ann.type(), ann));

		return map.values().toArray(new MonitoredOperation[0]);
	}
}
