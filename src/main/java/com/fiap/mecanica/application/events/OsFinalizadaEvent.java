package com.fiap.mecanica.application.events;

import com.fiap.mecanica.domain.model.OrdemServico;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OsFinalizadaEvent extends ApplicationEvent {
  private final OrdemServico ordemServico;

  public OsFinalizadaEvent(Object source, OrdemServico ordemServico) {
    super(source);
    this.ordemServico = ordemServico;
  }
}
