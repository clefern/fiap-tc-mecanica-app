package com.fiap.mecanica.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fiap.mecanica.application.service.MecanicoService;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.presentation.dto.MecanicoRequest;
import com.fiap.mecanica.presentation.dto.MecanicoResponse;
import com.fiap.mecanica.presentation.mapper.MecanicoMapper;
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
class MecanicoControllerTest {

  @Mock private MecanicoService service;

  @Spy private MecanicoMapper mapper = new MecanicoMapper();

  @InjectMocks private MecanicoController controller;

  @Test
  @DisplayName("Deve criar mecânico")
  void shouldCreateMecanico() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    MecanicoRequest req = new MecanicoRequest();
    req.setNome("João");
    req.setCpf("529.982.247-25");
    req.setEmail("j@o.com");
    req.setEspecialidade("Motor");

    Mecanico m = new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Motor");
    m.setId(UUID.randomUUID());

    when(service.create(any(Mecanico.class))).thenReturn(m);

    ResponseEntity<MecanicoResponse> response = controller.create(req);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(m.getId());
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldGetById() {
    UUID id = UUID.randomUUID();
    Mecanico m =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Mecânica de Motor");
    m.setId(id);

    when(service.getById(id)).thenReturn(Optional.of(m));

    ResponseEntity<MecanicoResponse> response = controller.getById(id);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getId()).isEqualTo(id);
  }

  @Test
  @DisplayName("Deve buscar por CPF")
  void shouldGetByCpf() {
    String cpf = "529.982.247-25";
    Mecanico m = new Mecanico("João", CPF.of(cpf), Email.of("j@o.com"), "Mecânica de Motor");
    m.setId(UUID.randomUUID());

    when(service.getByCpf(cpf)).thenReturn(Optional.of(m));

    ResponseEntity<MecanicoResponse> response = controller.getByCpf(cpf);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getCpf()).isEqualTo(cpf.replaceAll("\\D", ""));
  }

  @Test
  @DisplayName("Deve retornar 404 se CPF não existir")
  void shouldReturn404WhenCpfNotFound() {
    String cpf = "529.982.247-25";
    when(service.getByCpf(cpf)).thenReturn(Optional.empty());

    ResponseEntity<MecanicoResponse> response = controller.getByCpf(cpf);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Deve retornar 404 se ID não existir")
  void shouldReturn404WhenIdNotFound() {
    UUID id = UUID.randomUUID();
    when(service.getById(id)).thenReturn(Optional.empty());

    ResponseEntity<MecanicoResponse> response = controller.getById(id);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Deve atualizar mecânico")
  void shouldUpdate() {
    UUID id = UUID.randomUUID();
    MecanicoRequest req = new MecanicoRequest();
    req.setNome("João Silva");
    req.setCpf("529.982.247-25");
    req.setEmail("novo@o.com");
    req.setEspecialidade("Freios");

    Mecanico m =
        new Mecanico("João Silva", CPF.of("529.982.247-25"), Email.of("novo@o.com"), "Freios");
    m.setId(id);

    when(service.update(eq(id), any(Mecanico.class))).thenReturn(m);

    ResponseEntity<MecanicoResponse> response = controller.update(id, req);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getNome()).isEqualTo("João Silva");
  }

  @Test
  @DisplayName("Deve deletar mecânico")
  void shouldDelete() {
    UUID id = UUID.randomUUID();
    ResponseEntity<Void> response = controller.delete(id);

    verify(service).delete(id);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  @DisplayName("Deve listar todos")
  void shouldListAll() {
    Mecanico m =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Mecânica de Motor");
    m.setId(UUID.randomUUID());

    Page<Mecanico> page = new PageImpl<>(List.of(m));
    when(service.getAll(any(Pageable.class))).thenReturn(page);

    ResponseEntity<Page<MecanicoResponse>> response = controller.getAll(PageRequest.of(0, 10));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
  }
}
