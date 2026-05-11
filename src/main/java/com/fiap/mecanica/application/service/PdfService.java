package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;

public interface PdfService {
  byte[] gerarOrcamentoPdf(
      Orcamento orcamento, OrdemServico ordemServico, Cliente cliente, Veiculo veiculo);
}
