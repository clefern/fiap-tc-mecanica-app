package com.fiap.mecanica.presentation.mapper;

import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.presentation.dto.VeiculoRequest;
import com.fiap.mecanica.presentation.dto.VeiculoResponse;
import org.springframework.stereotype.Component;

@Component
public class VeiculoMapper {

  public Veiculo toDomain(VeiculoRequest request) {
    if (request == null) {
      return null;
    }
    return new Veiculo(
        PlacaVeiculo.of(request.getPlaca()),
        request.getModelo(),
        request.getMarca(),
        request.getAno());
  }

  public VeiculoResponse toResponse(Veiculo veiculo) {
    if (veiculo == null) {
      return null;
    }
    VeiculoResponse response = new VeiculoResponse();
    response.setId(veiculo.getId());
    response.setPlaca(veiculo.getPlaca().value());
    response.setMarca(veiculo.getMarca());
    response.setModelo(veiculo.getModelo());
    response.setAno(veiculo.getAno());
    return response;
  }
}
