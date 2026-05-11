package com.fiap.mecanica.infra.config.security;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component("osSecurity")
public class OsSecurity {

  private static final Logger logger = LoggerFactory.getLogger(OsSecurity.class);

  private final OrdemServicoRepository repository;

  public OsSecurity(OrdemServicoRepository repository) {
    this.repository = repository;
  }

  public boolean canManage(Authentication auth, UUID osId) {
    boolean allowed = hasRole(auth, "ADMIN") || isOwner(auth, osId);
    if (!allowed) {
      logger.warn(
          "Access denied for manage OS {}: User {} is neither ADMIN nor owner",
          osId,
          auth.getName());
    }
    return allowed;
  }

  public boolean canWorkOn(Authentication auth, UUID osId) {
    if (!hasRole(auth, "MECANICO")) {
      logger.warn(
          "Access denied for work on OS {}: User {} does not have role MECANICO",
          osId,
          auth.getName());
      return false;
    }
    return isOwnerOrUnassigned(auth, osId);
  }

  public boolean canApprove(Authentication auth, UUID osId) {
    if (!hasRole(auth, "CLIENTE")) {
      logger.warn(
          "Access denied for approve OS {}: User {} does not have role CLIENTE",
          osId,
          auth.getName());
      return false;
    }
    return isClientOwner(auth, osId);
  }

  private boolean isOwner(Authentication auth, UUID osId) {
    return repository
        .findById(osId)
        .map(
            os -> {
              UUID userId = getUserId(auth);
              boolean isOwner =
                  (userId != null)
                      && (userId.equals(os.getMecanicoExecucaoId())
                          || userId.equals(os.getMecanicoDiagnosticoId()));
              if (!isOwner) {
                logger.debug(
                    "User {} is not owner of OS {}. Diag: {}, Exec: {}",
                    userId,
                    osId,
                    os.getMecanicoDiagnosticoId(),
                    os.getMecanicoExecucaoId());
              }
              return isOwner;
            })
        .orElseGet(
            () -> {
              logger.warn("OS {} not found during ownership check", osId);
              return false;
            });
  }

  private boolean isOwnerOrUnassigned(Authentication auth, UUID osId) {
    return repository
        .findById(osId)
        .map(
            os -> {
              // Se já está finalizada, ninguém mexe (exceto leitura)
              if (isFinalState(os.getStatus())) {
                logger.warn(
                    "Access denied for work on OS {}: Status {} is final", osId, os.getStatus());
                return false;
              }
              UUID userId = getUserId(auth);
              UUID currentMechanicId;
              String phase;

              if (os.getStatus() == StatusOS.RECEBIDA
                  || os.getStatus() == StatusOS.EM_DIAGNOSTICO
                  || os.getStatus() == StatusOS.AGUARDANDO_APROVACAO) {
                currentMechanicId = os.getMecanicoDiagnosticoId();
                phase = "diagnostic";
              } else {
                currentMechanicId = os.getMecanicoExecucaoId();
                phase = "execution";
              }

              // Se não tem dono, pode pegar (self-assignment no service)
              if (currentMechanicId == null) {
                logger.debug("Access allowed: OS {} is unassigned for {} phase", osId, phase);
                return true;
              }
              // Se tem dono, tem que ser eu
              boolean isOwner = userId != null && userId.equals(currentMechanicId);
              if (!isOwner) {
                logger.warn(
                    "Access denied: OS {} belongs to {} mechanic {}, but user is {}",
                    osId,
                    phase,
                    currentMechanicId,
                    userId);
              }
              return isOwner;
            })
        .orElseGet(
            () -> {
              logger.warn("OS {} not found during work check", osId);
              return false;
            });
  }

  private boolean isClientOwner(Authentication auth, UUID osId) {
    return repository
        .findById(osId)
        .map(
            os -> {
              UUID userId = getUserId(auth);
              boolean isOwner = userId != null && userId.equals(os.getClienteId());
              if (!isOwner) {
                logger.warn(
                    "Access denied: OS {} belongs to client {}, but user is {}",
                    osId,
                    os.getClienteId(),
                    userId);
              }
              return isOwner;
            })
        .orElseGet(
            () -> {
              logger.warn("OS {} not found during client check", osId);
              return false;
            });
  }

  private boolean hasRole(Authentication auth, String role) {
    return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + role));
  }

  private UUID getUserId(Authentication auth) {
    if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
      return userDetails.getUser().getId();
    }
    return null;
  }

  private boolean isFinalState(StatusOS status) {
    return status == StatusOS.FINALIZADA
        || status == StatusOS.ENTREGUE
        || status == StatusOS.CANCELADA;
  }
}
