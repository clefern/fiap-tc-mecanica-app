package com.fiap.mecanica.infra.config.security;

import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Documento;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

  private final ClienteRepository clienteRepository;

  public SecurityService(ClienteRepository clienteRepository) {
    this.clienteRepository = clienteRepository;
  }

  public boolean isOwnerByDocumento(Authentication authentication, String documento) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    String principalEmail = getPrincipalEmail(authentication);
    Documento doc = createDocumento(documento);
    Optional<Cliente> cliente = clienteRepository.findByDocumento(doc);

    return cliente.map(c -> c.getEmail().value().equals(principalEmail)).orElse(false);
  }

  private Documento createDocumento(String doc) {
    // Tenta criar CPF, se falhar tenta CNPJ (ou lógica simplificada baseada em
    // tamanho)
    String raw = doc.replaceAll("\\D", "");
    if (raw.length() > 11) {
      return CNPJ.of(doc);
    }
    return CPF.of(doc);
  }

  private String getPrincipalEmail(Authentication authentication) {
    if (authentication.getPrincipal() instanceof UserDetails) {
      return ((UserDetails) authentication.getPrincipal()).getUsername();
    }
    return authentication.getPrincipal().toString();
  }
}
