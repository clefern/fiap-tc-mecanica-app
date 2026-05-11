package com.fiap.mecanica.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.service.AtendenteService;
import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.presentation.dto.AtendenteRequest;
import com.fiap.mecanica.presentation.dto.AtendenteResponse;
import com.fiap.mecanica.presentation.mapper.AtendenteMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class AtendenteControllerTest {

  @Mock private AtendenteService service;

  @Spy private AtendenteMapper mapper = new AtendenteMapper();

  @InjectMocks private AtendenteController controller;

  @Test
  @DisplayName("Deve criar atendente")
  void shouldCreateAtendente() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    AtendenteRequest req = new AtendenteRequest();
    req.setNome("Maria");
    req.setCpf("529.982.247-25");
    req.setEmail("m@o.com");

    Atendente a = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    a.setId(UUID.randomUUID());

    when(service.create(any(Atendente.class))).thenReturn(a);

    ResponseEntity<AtendenteResponse> response = controller.create(req);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(a.getId());
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldGetById() {
    UUID id = UUID.randomUUID();
    Atendente a = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    a.setId(id);

    when(service.getById(id)).thenReturn(Optional.of(a));

    ResponseEntity<AtendenteResponse> response = controller.getById(id);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getId()).isEqualTo(id);
  }

  @Test
  @DisplayName("Deve buscar por CPF")
  void shouldGetByCpf() {
    String cpf = "529.982.247-25";
    Atendente a = new Atendente("Maria", CPF.of(cpf), Email.of("m@o.com"));
    a.setId(UUID.randomUUID());

    when(service.getByCpf(cpf)).thenReturn(Optional.of(a));

    ResponseEntity<AtendenteResponse> response = controller.getByCpf(cpf);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getCpf()).isEqualTo(cpf.replaceAll("\\D", ""));
  }

  @Test
  @DisplayName("Deve retornar 404 se CPF não existir")
  void shouldReturn404WhenCpfNotFound() {
    String cpf = "529.982.247-25";
    when(service.getByCpf(cpf)).thenReturn(Optional.empty());

    ResponseEntity<AtendenteResponse> response = controller.getByCpf(cpf);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Deve retornar 404 se ID não existir")
  void shouldReturn404WhenIdNotFound() {
    UUID id = UUID.randomUUID();
    when(service.getById(id)).thenReturn(Optional.empty());

    ResponseEntity<AtendenteResponse> response = controller.getById(id);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Deve atualizar atendente")
  void shouldUpdate() {
    UUID id = UUID.randomUUID();
    AtendenteRequest req = new AtendenteRequest();
    req.setNome("Maria Silva");
    req.setCpf("529.982.247-25");
    req.setEmail("novo@o.com");

    Atendente a = new Atendente("Maria Silva", CPF.of("529.982.247-25"), Email.of("novo@o.com"));
    a.setId(id);

    when(service.update(eq(id), any(Atendente.class))).thenReturn(a);

    ResponseEntity<AtendenteResponse> response = controller.update(id, req);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getNome()).isEqualTo("Maria Silva");
  }

  @Test
  @DisplayName("Deve deletar atendente")
  void shouldDelete() {
    UUID id = UUID.randomUUID();
    ResponseEntity<Void> response = controller.delete(id);

    verify(service).delete(id);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  @DisplayName("Deve listar todos")
  void shouldListAll() {
    Atendente a = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    a.setId(UUID.randomUUID());

    Page<Atendente> page = new PageImpl<>(List.of(a));
    when(service.getAll(any(Pageable.class))).thenReturn(page);

    ResponseEntity<Page<AtendenteResponse>> response = controller.getAll(PageRequest.of(0, 10));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
  }
}
