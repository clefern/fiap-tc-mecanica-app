package com.fiap.mecanica.presentation.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.presentation.dto.AtendenteRequest;
import com.fiap.mecanica.presentation.dto.AtendenteResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtendenteMapperTest {

  private AtendenteMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new AtendenteMapper();
  }

  @Test
  void toDomain_ShouldReturnNull_WhenRequestIsNull() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  void toResponse_ShouldReturnNull_WhenDomainIsNull() {
    assertNull(mapper.toResponse(null));
  }

  @Test
  void toDomain() {
    AtendenteRequest request = new AtendenteRequest();
    request.setNome("Maria Silva");
    request.setCpf("39053344705");
    request.setEmail("maria@test.com");

    Atendente domain = mapper.toDomain(request);

    assertNotNull(domain);
    assertEquals(request.getNome(), domain.getNome());
    assertEquals(request.getCpf(), domain.getCpf().valor());
    assertEquals(request.getEmail(), domain.getEmail().value());
  }

  @Test
  void toResponse() {
    Atendente domain =
        new Atendente("Maria Silva", CPF.of("39053344705"), Email.of("maria@test.com"));
    domain.setId(UUID.randomUUID());

    AtendenteResponse response = mapper.toResponse(domain);

    assertNotNull(response);
    assertEquals(domain.getId(), response.getId());
    assertEquals(domain.getNome(), response.getNome());
    assertEquals(domain.getCpf().valor(), response.getCpf());
    assertEquals(domain.getEmail().value(), response.getEmail());
    assertEquals(domain.isAtivo(), response.isAtivo());
  }
}
