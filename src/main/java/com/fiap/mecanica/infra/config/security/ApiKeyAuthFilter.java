package com.fiap.mecanica.infra.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
  private static final String API_KEY_HEADER = "X-Api-Key";
  private static final String INTEGRATION_PATH_PREFIX = "/api/integracoes/";

  @Value("${mecanica.integrations.api-key}")
  private String configuredApiKey;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if (!request.getRequestURI().startsWith(INTEGRATION_PATH_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    // GET requests with a ?token= parameter use action token auth (validated in the controller)
    if ("GET".equalsIgnoreCase(request.getMethod()) && request.getParameter("token") != null) {
      filterChain.doFilter(request, response);
      return;
    }

    String providedKey = request.getHeader(API_KEY_HEADER);
    if (providedKey == null || !configuredApiKey.equals(providedKey)) {
      log.warn("[API_KEY_AUTH] Acesso negado para URI={}", request.getRequestURI());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"API key inválida ou ausente\"}");
      return;
    }

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            "integration-client", null, List.of(new SimpleGrantedAuthority("ROLE_INTEGRATION")));
    SecurityContextHolder.getContext().setAuthentication(auth);
    log.debug(
        "[API_KEY_AUTH] Autenticação de integração concedida para URI={}", request.getRequestURI());

    filterChain.doFilter(request, response);
  }
}
