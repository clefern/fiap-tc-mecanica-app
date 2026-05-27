package com.fiap.mecanica.infra.monitoring;

import com.fiap.mecanica.application.service.impl.OsLifecycleServiceImpl;
import com.fiap.mecanica.domain.model.OrdemServico;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.fiap.mecanica.infra.monitoring.MonitoredOperationType.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MonitoredOperationAspectTest extends Assertions {
	static final String FUNCTION_NAME = "iniciarDiagnostico";
	static final Class<?>[] PARAM_TYPES = new Class<?>[]{ UUID.class, UUID.class };

	final MeterRegistry registry = new SimpleMeterRegistry();
	final MonitoredOperationAspect aspect = new MonitoredOperationAspect(registry);

	@MonitoredOperation(type = OS_CREATED)
	@MonitoredOperation(type = OS_CYCLE_TIME)
	interface AnnotatedTarget {
		OrdemServico iniciarDiagnostico(UUID id, UUID mechId);
	}

	@Test
	void observabilityTest() throws Exception {
		final var target = mock(AnnotatedTarget.class);
		doAnswer(invocation -> {
			Thread.sleep(1100L);
			return Instancio.create(OrdemServico.class);
		}).when(target).iniciarDiagnostico(any(), any());

		final var method = AnnotatedTarget.class.getMethod(FUNCTION_NAME, PARAM_TYPES);
		final var invocation = new ProxyMethodInvocationMock(
			target,
			method,
			new Object[]{ UUID.randomUUID(), UUID.randomUUID() }
		);

		final var data = assertDoesNotThrow(() -> aspect.observability(
			new MethodInvocationProceedingJoinPoint(invocation)
		));

		assertInstanceOf(OrdemServico.class, data);

		{
			final var metric = registry.find(OS_CREATED.value());
			assertNotNull(metric);
			final var counter = metric.counter();
			assertNotNull(counter);
			assertEquals(1.0, counter.count());
		}
		{
			final var metric = registry.find(OS_CYCLE_TIME.value());
			assertNotNull(metric);
			final var timer = metric.timer();
			assertNotNull(timer);
			assertTrue(timer.totalTime(TimeUnit.SECONDS) >= 1L);
		}
	}

	@Test
	void observabilityErrorTest() throws Exception {
		final var target = mock(AnnotatedTarget.class);
		doThrow(new RuntimeException("batata")).when(target).iniciarDiagnostico(any(), any());

		final var method = AnnotatedTarget.class.getMethod(FUNCTION_NAME, PARAM_TYPES);
		final var invocation = new ProxyMethodInvocationMock(
			target,
			method,
			new Object[]{ UUID.randomUUID(), UUID.randomUUID() }
		);

		// The aspect rethrows the exception, but ProxyMethodInvocationMock (using method.invoke) 
		// wraps it in InvocationTargetException.
		assertThrows(java.lang.reflect.InvocationTargetException.class, () -> aspect.observability(
			new MethodInvocationProceedingJoinPoint(invocation)
		));

		// O aspect sempre incrementa APPLICATION_ERROR em qualquer erro (linha 73 do
		// MonitoredOperationAspect). APPLICATION_INTEGRATION_ERROR é específico para
		// adapters de integração (BaseRepository/EmailSender/NotificationService/
		// PdfService) — o mock genérico AnnotatedTarget deste teste não é nenhum
		// desses, portanto não dispara o counter de integração. Para validar a
		// contagem de erro em qualquer target, usamos o counter universal.
		{
			final var metric = registry.find(APPLICATION_ERROR.value());
			assertNotNull(metric);
			final var counter = metric.counter();
			assertNotNull(counter);
			assertEquals(1D, counter.count());
		}
	}
}
