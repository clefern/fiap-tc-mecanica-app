package com.fiap.mecanica.application.events;

import com.fiap.mecanica.domain.model.OrdemServico;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrdemServicoCanceladaEvent extends ApplicationEvent {
  private final OrdemServico ordemServico;

  public OrdemServicoCanceladaEvent(Object source, OrdemServico ordemServico) {
    super(source);
    this.ordemServico = ordemServico;
  }
}
