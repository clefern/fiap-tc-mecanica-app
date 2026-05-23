package com.fiap.mecanica.infra.monitoring;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MonitoredOperationType {
	MECHANIC_PERFORMANCE("mechanic_performance"),

	EMAIL_SEND_OS_QUOTE("email_send_os_quote"),
	EMAIL_SEND_OS_CONFIRMATION("email_send_os_confirmation"),
	EMAIL_DELIVERY_RATE("email_delivery_rate"),

	INVENTORY_REMOVED("inventory_removed"),
	INVENTORY_ADDED("inventory_added"),
	INVENTORY_UPDATED("inventory_updated"),
	INVENTORY_TURNOVER("inventory_turnover"),

	OS_DIAGNOSIS_STARTED("os_diagnosis_started"),
	OS_DIAGNOSIS_COMPLETED("os_diagnosis_completed"),
	OS_MECHANIC_CHANGED("os_mechanic_changed"),
	OS_CREATED("os_created"),
	OS_STARTED("os_started"),
	OS_COMPLETED("os_completed"),
	OS_DELIVERED("os_delivered"),
	OS_APPROVED("os_approved"),
	OS_CANCELLED("os_cancelled"),
	OS_CYCLE_TIME("os_cycle_time"),
	OS_CONVERSION_RATE("os_conversion_rate"),

	BUDGET_CREATED("budget_created"),
	BUDGET_APPROVED("budget_approved"),
	BUDGET_REPROVED("budget_reproved"),
	BUDGET_CANCELLED("budget_cancelled"),

	REPORT_MECHANIC("report_mechanic"),
	REPORT_OS_AVERAGE_EXECUTION_TIME("report_os_average_execution_time"),
	REPORT_PDF_GENERATION_TIME("report_pdf_generation_time"),

	APPLICATION_INTEGRATION_ERROR("application_integration_error"),
	APPLICATION_ERROR("application_error"),
	APPLICATION_LATENCY_API("application_latency_api"),
	APPLICATION_PERFORMANCE_DB_QUERY("application_performance_db_query"),

	// used for tests
	TEST_A("test_a"),
	TEST_B("test_b");

	private final String value;

	public final String value() {
		return this.value;
	}
}
