package com.fiap.mecanica.infra.monitoring;

import java.lang.annotation.*;

@Repeatable(MonitoredOperations.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface MonitoredOperation {

	MonitoredOperationType type();
}
