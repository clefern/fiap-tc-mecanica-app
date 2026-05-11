package com.fiap.mecanica.application.service.os;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OsMecanicoAssigner {

  public void assign(OrdemServico os, UUID mecanicoId) {
    if (mecanicoId == null) {
      return;
    }

    if (os.getStatus() == StatusOS.RECEBIDA
        || os.getStatus() == StatusOS.EM_DIAGNOSTICO
        || os.getStatus() == StatusOS.AGUARDANDO_APROVACAO) {
      if (os.getMecanicoDiagnosticoId() == null) {
        os.atribuirMecanicoDiagnostico(mecanicoId);
      }
    } else {
      if (os.getMecanicoExecucaoId() == null) {
        os.atribuirMecanicoExecucao(mecanicoId);
      }
    }
  }
}
