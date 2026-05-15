package com.fiap.mecanica.infra.config.security;

import com.fiap.mecanica.infra.config.CorrelationIdFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final ApiKeyAuthFilter apiKeyAuthFilter;
  private final CorrelationIdFilter correlationIdFilter;
  private final UserDetailsService userDetailsService;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthFilter,
      ApiKeyAuthFilter apiKeyAuthFilter,
      CorrelationIdFilter correlationIdFilter,
      UserDetailsService userDetailsService) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.apiKeyAuthFilter = apiKeyAuthFilter;
    this.correlationIdFilter = correlationIdFilter;
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public endpoints
                    .requestMatchers(
                        "/auth/**",
                        "/oauth/**", // New OAuth endpoints
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**"
										)
                    .permitAll()
                    // Email action token flow: GET with ?token= — no JWT or API key required
                    .requestMatchers(HttpMethod.GET, "/api/integracoes/orcamentos/aprovacao")
                    .permitAll()
                    // Protected endpoints
                    .anyRequest()
                    .authenticated())
        .headers(
            headers ->
                headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")))
        .exceptionHandling(
            e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

	@Bean
  public AuthenticationProvider authenticationProvider(final PasswordEncoder encoder) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(encoder);
    return authProvider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
