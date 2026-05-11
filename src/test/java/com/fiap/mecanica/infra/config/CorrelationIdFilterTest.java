package com.fiap.mecanica.infra.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @Test
  @DisplayName("Should generate correlation ID when not provided in request")
  void shouldGenerateCorrelationId() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(
        request,
        response,
        (req, res) -> {
          String mdcValue = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
          assertThat(mdcValue).isNotNull().isNotBlank();
        });

    String header = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
    assertThat(header).isNotNull().isNotBlank();
  }

  @Test
  @DisplayName("Should reuse correlation ID from request header")
  void shouldReuseProvidedCorrelationId() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "test-corr-123");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(
        request,
        response,
        (req, res) -> {
          String mdcValue = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
          assertThat(mdcValue).isEqualTo("test-corr-123");
        });

    assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
        .isEqualTo("test-corr-123");
  }

  @Test
  @DisplayName("Should clean MDC after request completes")
  void shouldCleanMdcAfterRequest() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
  }

  @Test
  @DisplayName("Should clean MDC even when filter chain throws exception")
  void shouldCleanMdcOnException() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    try {
      filter.doFilterInternal(
          request,
          response,
          (req, res) -> {
            throw new RuntimeException("simulated error");
          });
    } catch (RuntimeException ignored) {
    }

    assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
  }
}
