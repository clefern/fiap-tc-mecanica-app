package com.fiap.mecanica.application.events;

import com.fiap.mecanica.domain.model.OrdemServico;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OsCriadaEvent extends ApplicationEvent {
  private final OrdemServico ordemServico;

  public OsCriadaEvent(Object source, OrdemServico ordemServico) {
    super(source);
    this.ordemServico = ordemServico;
  }
}
