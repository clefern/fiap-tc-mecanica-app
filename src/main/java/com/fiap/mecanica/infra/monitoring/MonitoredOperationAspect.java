package com.fiap.mecanica.infra.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MonitoredOperationAspect {

  private final MeterRegistry meterRegistry;

  public MonitoredOperationAspect(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Around("@annotation(monitoredOperation)")
  public Object around(ProceedingJoinPoint pjp, MonitoredOperation monitoredOperation)
      throws Throwable {
    String operation = monitoredOperation.value();
    String serviceName = pjp.getTarget().getClass().getSimpleName();

    Timer.Sample sample = Timer.start(meterRegistry);
    String status = "success";

    try {
      return pjp.proceed();
    } catch (Exception ex) {
      status = "error";
      throw ex;
    } finally {
      sample.stop(
          Timer.builder("mecanica.service.execution")
              .tag("service", serviceName)
              .tag("operation", operation)
              .tag("status", status)
              .register(meterRegistry));
    }
  }
}
