package com.fiap.mecanica.presentation.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.presentation.dto.MecanicoRequest;
import com.fiap.mecanica.presentation.dto.MecanicoResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MecanicoMapperTest {

  private MecanicoMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new MecanicoMapper();
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
    MecanicoRequest request = new MecanicoRequest();
    request.setNome("João Silva");
    request.setCpf("39053344705");
    request.setEmail("joao@test.com");
    request.setEspecialidade("Motor");

    Mecanico domain = mapper.toDomain(request);

    assertNotNull(domain);
    assertEquals(request.getNome(), domain.getNome());
    assertEquals(request.getCpf(), domain.getCpf().valor());
    assertEquals(request.getEmail(), domain.getEmail().value());
    assertEquals(request.getEspecialidade(), domain.getEspecialidade());
  }

  @Test
  void toResponse() {
    Mecanico domain =
        new Mecanico("João Silva", CPF.of("39053344705"), Email.of("joao@test.com"), "Motor");
    domain.setId(UUID.randomUUID());

    MecanicoResponse response = mapper.toResponse(domain);

    assertNotNull(response);
    assertEquals(domain.getId(), response.getId());
    assertEquals(domain.getNome(), response.getNome());
    assertEquals(domain.getCpf().valor(), response.getCpf());
    assertEquals(domain.getEmail().value(), response.getEmail());
    assertEquals(domain.getEspecialidade(), response.getEspecialidade());
    assertEquals(domain.isAtivo(), response.isAtivo());
  }
}
