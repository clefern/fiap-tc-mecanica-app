package com.fiap.mecanica.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ClienteRepositoryIntegrationTest {

  @MockitoBean private JavaMailSender javaMailSender;

  @Autowired private ClienteRepository repository;

  @Test
  @DisplayName("Deve salvar cliente e buscar por Documento")
  void saveAndFindByDocumento() {
    Cliente cliente = novoCliente("39053344705", "João");
    repository.save(cliente);

    Optional<Cliente> found = repository.findByDocumento(CPF.of("39053344705"));
    assertThat(found).isPresent();
    assertThat(found.get().getNome()).isEqualTo("João");
    assertThat(found.get().getDocumento().valor()).isEqualTo("39053344705");
  }

  @Test
  @DisplayName("existsByDocumento retorna true se existe")
  void existsByDocumento() {
    Cliente cliente = novoCliente("52998224725", "Maria");
    repository.save(cliente);

    boolean exists = repository.existsByDocumento(CPF.of("52998224725"));
    assertThat(exists).isTrue();
  }

  private Cliente novoCliente(String doc, String nome) {
    return new Cliente(
        nome,
        CPF.of(doc),
        TipoPessoa.FISICA,
        Email.of("email@teste.com"),
        TelefoneBr.of("11999999999"),
        Endereco.of("Rua X"));
  }
}
