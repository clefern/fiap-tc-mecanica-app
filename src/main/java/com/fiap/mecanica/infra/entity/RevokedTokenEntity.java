package com.fiap.mecanica.infra.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "revoked_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokedTokenEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, columnDefinition = "TEXT")
  private String token;

  @Column(name = "revoked_at", nullable = false)
  @Builder.Default
  private LocalDateTime revokedAt = LocalDateTime.now();

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;
}
