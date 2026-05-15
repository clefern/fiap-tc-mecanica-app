package com.fiap.mecanica.infra.monitoring;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

public final class MonitoredOperationsMock {
	private MonitoredOperationsMock() {
		// ignored
	}

	public static MonitoredOperations annotations(final List<MonitoredOperationType> types) {
		Objects.requireNonNull(types, "property 'types' could not be null");
		final var arr = new MonitoredOperation[types.size()];
		for (var i = 0; i < types.size(); i++) {
			final var type = types.get(i);
			arr[i] = new MonitoredOperation() {

				@Override
				public Class<? extends Annotation> annotationType() {
					return MonitoredOperation.class;
				}

				@Override
				public MonitoredOperationType type() {
					return type;
				}
			};
		}

		return new MonitoredOperations() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return MonitoredOperations.class;
			}

			@Override
			public MonitoredOperation[] value() {
				return arr;
			}
		};
	}
}
