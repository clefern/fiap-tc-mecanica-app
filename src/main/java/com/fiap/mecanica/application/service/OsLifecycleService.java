package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.UUID;

public interface OsLifecycleService {
  OrdemServico iniciarDiagnostico(UUID id, UUID mecanicoId);

  OrdemServico trocarMecanicoResponsavel(UUID id, UUID novoMecanicoId);

  OrdemServico finalizarDiagnostico(UUID id, UUID mecanicoId);

  OrdemServico iniciarExecucao(UUID id, UUID mecanicoId);

  OrdemServico finalizar(UUID id, UUID mecanicoId);

  OrdemServico entregar(UUID id);

  OrdemServico cancelar(UUID id);

  OrdemServico aprovarOS(UUID id);
}
