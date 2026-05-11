package com.fiap.mecanica.domain.model;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.valueobject.Documento;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import java.util.*;
import lombok.*;

/**
 * Entidade de domínio Cliente. Regras: - Nome obrigatório e não vazio - Documento obrigatório (CPF
 * ou CNPJ) - Pelo menos um contato (Email ou Telefone)? Não, ambos obrigatórios na regra atual.
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Cliente extends User {

  @EqualsAndHashCode.Include private Documento documento;

  private TipoPessoa tipo;
  private TelefoneBr telefone;
  private Endereco endereco;

  // Mapa de veículos para garantir unicidade por placa dentro do cliente
  private Map<String, Veiculo> veiculosPorPlaca = new HashMap<>();

  public Cliente(
      String nome,
      Documento documento,
      TipoPessoa tipo,
      Email email,
      TelefoneBr telefone,
      Endereco endereco) {
    this.nome = nome;
    this.documento = documento;
    this.tipo = tipo;
    this.email = email;
    this.telefone = telefone;
    this.endereco = endereco;
    this.ativo = true;
    this.role = UserRole.CLIENTE;
    validate();
  }

  private void validate() {
    if (nome == null || nome.trim().isEmpty()) {
      throw new IllegalArgumentException("Nome é obrigatório");
    }
    if (documento == null) {
      throw new IllegalArgumentException("Documento é obrigatório");
    }
    if (tipo == null) {
      throw new IllegalArgumentException("Tipo de pessoa é obrigatório");
    }
    if (email == null) {
      throw new IllegalArgumentException("Email é obrigatório");
    }
    if (telefone == null) {
      throw new IllegalArgumentException("Telefone é obrigatório");
    }
    if (endereco == null) {
      throw new IllegalArgumentException("Endereço é obrigatório");
    }
  }

  public void adicionarVeiculo(Veiculo veiculo) {
    if (veiculo == null) {
      throw new IllegalArgumentException("Veículo não pode ser nulo");
    }
    veiculosPorPlaca.put(veiculo.getPlaca().value(), veiculo);
  }

  public void removerVeiculo(PlacaVeiculo placa) {
    if (placa == null) {
      throw new IllegalArgumentException("Placa não pode ser nula");
    }
    veiculosPorPlaca.remove(placa.value());
  }

  public List<Veiculo> getVeiculos() {
    return new ArrayList<>(veiculosPorPlaca.values());
  }
}
