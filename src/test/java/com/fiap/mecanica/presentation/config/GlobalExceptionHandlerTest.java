package com.fiap.mecanica.presentation.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.exception.BusinessException;
import com.fiap.mecanica.domain.exception.DomainRuleException;
import com.fiap.mecanica.domain.exception.DuplicateDocumentoException;
import com.fiap.mecanica.domain.exception.DuplicatePlacaException;
import com.fiap.mecanica.domain.exception.ItemDuplicadoException;
import com.fiap.mecanica.domain.exception.ResourceNotFoundException;
import com.fiap.mecanica.domain.exception.SystemException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @InjectMocks private GlobalExceptionHandler handler;

  private static class TestResourceNotFoundException extends ResourceNotFoundException {
    protected TestResourceNotFoundException(String message, String code) {
      super(message, code);
    }
  }

  private static class TestDomainRuleException extends DomainRuleException {
    protected TestDomainRuleException(String message, String code) {
      super(message, code);
    }
  }

  private static class TestBusinessException extends BusinessException {
    protected TestBusinessException(String message, String code) {
      super(message, code);
    }
  }

  private static class TestSystemException extends SystemException {
    protected TestSystemException(String message, String code) {
      super(message, code);
    }
  }

  @Test
  @DisplayName("Should handle ResourceNotFoundException")
  void handleResourceNotFoundException() {
    ResourceNotFoundException ex =
        new TestResourceNotFoundException("Resource not found", "NOT_FOUND");
    ResponseEntity<Object> response = handler.handleResourceNotFound(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Resource not found");
    assertThat(body).containsEntry("code", "NOT_FOUND");
  }

  @Test
  @DisplayName("Should handle DuplicateDocumentoException")
  void handleDuplicateDocumentoException() {
    DuplicateDocumentoException ex = new DuplicateDocumentoException("CPF duplicate");
    ResponseEntity<Object> response = handler.handleDuplicateDocumento(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Documento já cadastrado: CPF duplicate");
  }

  @Test
  @DisplayName("Should handle DuplicatePlacaException")
  void handleDuplicatePlacaException() {
    DuplicatePlacaException ex = new DuplicatePlacaException("Placa duplicate");
    ResponseEntity<Object> response = handler.handleDuplicatePlaca(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Placa já cadastrada: Placa duplicate");
  }

  @Test
  @DisplayName("Should handle DomainRuleException (Unprocessable Entity)")
  void handleDomainRuleException() {
    DomainRuleException ex = new TestDomainRuleException("Rule broken", "RULE-001");
    ResponseEntity<Object> response = handler.handleDomainRule(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Rule broken");
    assertThat(body).containsEntry("code", "RULE-001");
  }

  @Test
  void deveRetornarUnprocessableEntityQuandoDomainRuleExceptionSemCodigo() {
    DomainRuleException ex = new TestDomainRuleException("Erro de regra de negocio", null);

    ResponseEntity<Object> response = handler.handleDomainRule(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isInstanceOf(Map.class);
    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Erro de regra de negocio");
    assertThat(body).doesNotContainKey("code");
  }

  @Test
  void deveRetornarConflictQuandoDomainRuleExceptionComCodigo409() {
    DomainRuleException ex = new TestDomainRuleException("Rule conflict", "ERR-409-CONFLICT");
    ResponseEntity<Object> response = handler.handleDomainRule(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  @DisplayName("Should handle BusinessException as 422 when code does not contain -409-")
  void handleBusinessException() {
    BusinessException ex = new TestBusinessException("Business error", "BUS-001");
    ResponseEntity<Object> response = handler.handleBusinessException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Business error");
    assertThat(body).containsEntry("code", "BUS-001");
  }

  @Test
  @DisplayName("Should handle BusinessException as 409 when code contains -409-")
  void handleBusinessExceptionWith409Code() {
    BusinessException ex = new TestBusinessException("Conflict error", "OS-409-01");
    ResponseEntity<Object> response = handler.handleBusinessException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("code", "OS-409-01");
  }

  @Test
  @DisplayName("Should handle ItemDuplicadoException as 409 Conflict")
  void handleItemDuplicadoException() {
    ItemDuplicadoException ex =
        new ItemDuplicadoException("Troca de Óleo", java.util.UUID.randomUUID());
    ResponseEntity<Object> response = handler.handleBusinessException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("code", "OS-409-01");
    assertThat(body.get("error").toString()).contains("Troca de Óleo");
  }

  @Test
  @DisplayName("Should handle SystemException")
  void handleSystemException() {
    SystemException ex = new TestSystemException("System error", "SYS-001");
    ResponseEntity<Object> response = handler.handleSystemException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Erro interno do sistema. Contate o suporte.");
    assertThat(body).containsEntry("code", "SYS-001");
  }

  @Test
  @DisplayName("Should handle IllegalArgumentException")
  void handleIllegalArgumentException() {
    IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
    ResponseEntity<Object> response = handler.handleIllegalArgument(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Invalid argument");
    assertThat(body).containsEntry("code", "BAD_REQUEST");
  }

  @Test
  @DisplayName("Should handle MethodArgumentNotValidException")
  void handleMethodArgumentNotValidException() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("object", "field", "must not be null");
    when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

    MethodParameter parameter = mock(MethodParameter.class);
    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(parameter, bindingResult);

    ResponseEntity<Object> response = handler.handleValidationExceptions(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Map<String, String> errors = (Map<String, String>) response.getBody();
    assertThat(errors).containsEntry("field", "must not be null");
  }

  @Test
  @DisplayName("Should handle AccessDeniedException")
  void handleAccessDeniedException() {
    AccessDeniedException ex = new AccessDeniedException("Access denied");
    ResponseEntity<Object> response = handler.handleAccessDenied(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Access Denied");
    assertThat(body).containsEntry("code", "FORBIDDEN");
  }

  @Test
  @DisplayName("Should handle AuthenticationException")
  void handleAuthenticationException() {
    AuthenticationException ex = new AuthenticationException("Auth failed") {};
    ResponseEntity<Object> response = handler.handleAuthenticationException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Unauthorized");
    assertThat(body).containsEntry("code", "UNAUTHORIZED");
  }

  @Test
  @DisplayName("Should handle EntityNotFoundException")
  void handleEntityNotFoundException() {
    EntityNotFoundException ex = new EntityNotFoundException("Entity not found");
    ResponseEntity<Object> response = handler.handleEntityNotFound(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Entity not found");
    assertThat(body).containsEntry("code", "ENTITY_NOT_FOUND");
  }

  @Test
  @DisplayName("Should handle PropertyReferenceException")
  void handlePropertyReferenceException() {
    TypeInformation<?> typeInfo = mock(TypeInformation.class);
    PropertyReferenceException ex =
        new PropertyReferenceException("invalidProp", typeInfo, List.of());
    ResponseEntity<Object> response = handler.handlePropertyReferenceException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("error").toString()).contains("Propriedade de ordenação inválida");
    assertThat(body).containsEntry("code", "INVALID_SORT_PROPERTY");
  }

  @Test
  @DisplayName("Should handle InvalidDataAccessApiUsageException")
  void handleInvalidDataAccessApiUsageException() {
    InvalidDataAccessApiUsageException ex = new InvalidDataAccessApiUsageException("Invalid usage");
    ResponseEntity<Object> response = handler.handleInvalidDataAccessApiUsage(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Invalid usage");
    assertThat(body).containsEntry("code", "INVALID_DATA_ACCESS");
  }

  @Test
  @DisplayName("Should handle HttpRequestMethodNotSupportedException as 405")
  void handleMethodNotAllowed() {
    org.springframework.web.HttpRequestMethodNotSupportedException ex =
        new org.springframework.web.HttpRequestMethodNotSupportedException("DELETE");
    ResponseEntity<Object> response = handler.handleMethodNotAllowed(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("error").toString()).contains("DELETE");
    assertThat(body).containsEntry("code", "METHOD_NOT_ALLOWED");
  }

  @Test
  @DisplayName("Should handle HttpMediaTypeNotSupportedException as 415")
  void handleUnsupportedMediaType() {
    org.springframework.web.HttpMediaTypeNotSupportedException ex =
        new org.springframework.web.HttpMediaTypeNotSupportedException(
            MediaType.APPLICATION_XML, List.of(MediaType.APPLICATION_JSON));
    ResponseEntity<Object> response = handler.handleUnsupportedMediaType(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("error").toString()).contains("application/xml");
    assertThat(body).containsEntry("code", "UNSUPPORTED_MEDIA_TYPE");
  }

  @Test
  @DisplayName("Should handle generic Exception without exposing debug info")
  void handleGenericException() {
    Exception ex = new Exception("Unexpected error");
    ResponseEntity<Object> response = handler.handleGlobalException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("error", "Erro inesperado. Contate o suporte.");
    assertThat(body).containsEntry("code", "SYS-500");
    assertThat(body).doesNotContainKey("debug_message");
  }
}
