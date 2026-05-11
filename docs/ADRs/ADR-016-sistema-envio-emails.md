# ADR-016: Sistema de Envio de Emails e Notificações

## Status

Aceita (Atualizada)

## Contexto

O sistema de oficina mecânica gerencia o ciclo de vida de Ordens de Serviço (OS) e Orçamentos. Atualmente, identificamos que a comunicação com o cliente é passiva. Para melhorar a experiência do cliente e a transparência do processo, é necessário notificar o cliente proativamente em momentos chaves do fluxo de serviço.

Os momentos identificados como críticos para notificação são:

1.  **Criação da OS**: Confirmar que o veículo foi recebido e a solicitação registrada.
2.  **Geração do Orçamento**: Enviar o orçamento detalhado (com PDF) para aprovação.
3.  **Finalização do Serviço**: Avisar que o veículo está pronto para retirada.

O processo deve ser assíncrono para não impactar a performance das operações de escrita (POST/PUT).

## Decisão

Decidimos expandir a arquitetura de eventos já desenhada para cobrir todo o ciclo de vida da OS, utilizando **Spring Events** e **Spring Mail**.

### Arquitetura e Componentes

1.  **Modelo de Eventos (Domain Events)**:

    - `OsCriadaEvent`: Disparado quando a OS é salva pela primeira vez.
    - `OrcamentoGeradoEvent`: Disparado quando o orçamento é calculado.
    - `OsFinalizadaEvent`: Disparado quando o status da OS muda para FINALIZADA.

2.  **Listeners**:

    - `NotificacaoEmailListener`: Um listener coeso (ou listeners segregados por domínio) que escuta esses eventos e orquestra o envio.

3.  **Templates (Thymeleaf)**:

    - `email/os-criada.html`: Boas vindas, número da OS, dados do veículo, prazo estimado inicial.
    - `email/orcamento-gerado.html`: Detalhes de valores, link para aprovação (futuro), anexo PDF.
    - `email/os-finalizada.html`: Aviso de conclusão, total final, horários para retirada.

4.  **Serviço de Email**:

    - Expansão do `EmailService` para suportar novos tipos de mensagem sem acoplamento excessivo (ex: métodos específicos ou builder genérico).

5.  **Fluxo de Execução Genérico**:
    1.  Domínio executa ação (Cria OS / Finaliza OS).
    2.  Domínio publica Evento.
    3.  Listener (@Async) captura.
    4.  Listener carrega dados agregados se necessário (ex: Cliente).
    5.  Listener chama `EmailService`.
    6.  Email é despachado via SMTP.

## Análise de Alternativas

Mantemos a decisão de usar **Spring Mail** + **Thymeleaf** pela robustez e simplicidade já comprovadas na implementação do orçamento.

## Implementação

### Estrutura de Pacotes Sugerida

```
src/main/java/com/fiap/mecanica
├── application
│   ├── events
│   │   ├── OsCriadaEvent.java
│   │   ├── OrcamentoGeradoEvent.java
│   │   └── OsFinalizadaEvent.java
│   └── listeners
│       └── NotificacaoEmailListener.java
├── infra
│   ├── mail
│   │   ├── EmailService.java (Interface atualizada)
│   │   └── SmtpEmailService.java (Impl)
```

### Novos Templates

- `src/main/resources/templates/email/os-criada.html`
- `src/main/resources/templates/email/os-finalizada.html`

## Consequências

- **Positivas**: Aumento significativo na transparência e confiança do cliente; Redução de chamadas telefônicas perguntando "o carro está pronto?".
- **Negativas**: Aumento no tráfego de emails (custo em provedores como SendGrid); Necessidade de manter mais templates HTML.

## Próximos Passos

1. Criar eventos `OsCriadaEvent` e `OsFinalizadaEvent`.
2. Criar templates HTML correspondentes.
3. Atualizar `EmailService` para novos métodos de envio.
4. Implementar Listener para os novos eventos.
