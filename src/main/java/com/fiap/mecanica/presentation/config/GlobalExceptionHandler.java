package com.fiap.mecanica.presentation.config;

import com.fiap.mecanica.domain.exception.BusinessException;
import com.fiap.mecanica.domain.exception.DomainRuleException;
import com.fiap.mecanica.domain.exception.DuplicateDocumentoException;
import com.fiap.mecanica.domain.exception.DuplicatePlacaException;
import com.fiap.mecanica.domain.exception.ResourceNotFoundException;
import com.fiap.mecanica.domain.exception.SystemException;
import com.fiap.mecanica.infra.config.CorrelationIdFilter;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // --- Handlers da Hierarquia MecanicaException ---

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getCode());
  }

  @ExceptionHandler(DuplicateDocumentoException.class)
  public ResponseEntity<Object> handleDuplicateDocumento(DuplicateDocumentoException ex) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), ex.getCode());
  }

  @ExceptionHandler(DuplicatePlacaException.class)
  public ResponseEntity<Object> handleDuplicatePlaca(DuplicatePlacaException ex) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), ex.getCode());
  }

  @ExceptionHandler(DomainRuleException.class)
  public ResponseEntity<Object> handleDomainRule(DomainRuleException ex) {
    logger.warn("❌ Domain rule violation: {}", ex.getMessage());
    HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
    if (ex.getCode() != null && ex.getCode().contains("-409-")) {
      status = HttpStatus.CONFLICT;
    }
    return buildResponse(status, ex.getMessage(), ex.getCode());
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Object> handleBusinessException(BusinessException ex) {
    logger.warn("❌ Business rule violation: {}", ex.getMessage());
    HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
    if (ex.getCode() != null && ex.getCode().contains("-409-")) {
      status = HttpStatus.CONFLICT;
    }
    return buildResponse(status, ex.getMessage(), ex.getCode());
  }

  @ExceptionHandler(SystemException.class)
  public ResponseEntity<Object> handleSystemException(SystemException ex) {
    logger.error("❌ System error occurred: code={}", ex.getCode(), ex);
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Erro interno do sistema. Contate o suporte.",
        ex.getCode());
  }

  // --- Handlers Legados e Framework ---

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "BAD_REQUEST");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
    return buildResponse(HttpStatus.FORBIDDEN, "Access Denied", "FORBIDDEN");
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
    return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "UNAUTHORIZED");
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "ENTITY_NOT_FOUND");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Object> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
    return buildResponse(
        HttpStatus.METHOD_NOT_ALLOWED,
        "Método HTTP não suportado: " + ex.getMethod(),
        "METHOD_NOT_ALLOWED");
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Object> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
    return buildResponse(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "Tipo de mídia não suportado: " + ex.getContentType(),
        "UNSUPPORTED_MEDIA_TYPE");
  }

  @ExceptionHandler(PropertyReferenceException.class)
  public ResponseEntity<Object> handlePropertyReferenceException(PropertyReferenceException ex) {
    return buildResponse(
        HttpStatus.BAD_REQUEST,
        "Propriedade de ordenação inválida: " + ex.getPropertyName(),
        "INVALID_SORT_PROPERTY");
  }

  @ExceptionHandler(InvalidDataAccessApiUsageException.class)
  public ResponseEntity<Object> handleInvalidDataAccessApiUsage(
      InvalidDataAccessApiUsageException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_DATA_ACCESS");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleGlobalException(Exception ex) {
    logger.error("Unexpected error occurred", ex);
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado. Contate o suporte.", "SYS-500");
  }

  private ResponseEntity<Object> buildResponse(HttpStatus status, String message, String code) {
    Map<String, Object> body = new HashMap<>();
    body.put("error", message);
    if (code != null) {
      body.put("code", code);
    }
    String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
    if (correlationId != null) {
      body.put("correlationId", correlationId);
    }
    return ResponseEntity.status(status).body(body);
  }
}
