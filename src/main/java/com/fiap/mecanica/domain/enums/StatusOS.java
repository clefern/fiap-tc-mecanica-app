package com.fiap.mecanica.domain.enums;

public enum StatusOS {
  RECEBIDA, // Antiga ABERTA
  EM_DIAGNOSTICO, // Antiga DIAGNOSTICO
  AGUARDANDO_APROVACAO,
  APROVADA,
  EM_EXECUCAO,
  FINALIZADA,
  ENTREGUE,
  CANCELADA // Mantido para casos de cancelamento
}
