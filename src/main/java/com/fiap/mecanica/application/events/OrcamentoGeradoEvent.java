package com.fiap.mecanica.application.events;

import com.fiap.mecanica.domain.model.Orcamento;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrcamentoGeradoEvent extends ApplicationEvent {
  private final Orcamento orcamento;

  public OrcamentoGeradoEvent(Object source, Orcamento orcamento) {
    super(source);
    this.orcamento = orcamento;
  }
}
