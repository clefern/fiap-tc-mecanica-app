# ADR-007: Plano de Reforço de Validação (Camada de Apresentação)

## Contexto
Após uma análise minuciosa da camada de apresentação (DTOs) solicitada para verificar a integridade dos dados e a conformidade com o ADR-006, identificamos lacunas críticas na aplicação do princípio *Fail-Fast*. Embora a arquitetura de delegação para Value Objects esteja correta, a implementação dos contratos de entrada (DTOs) está permissiva em pontos chave, expondo o sistema a riscos de segurança e inconsistência de dados.

## Decisão
Implementar um plano de reforço imediato nas validações de DTOs, focado em fechar brechas de integridade e segurança. As correções serão aplicadas diretamente na camada de apresentação, mantendo a regra de **não duplicar lógica de domínio**, mas garantindo que contratos básicos (presença, formato estrutural, segurança) sejam validados antes de acionar a camada de serviço.

## Detalhes da Implementação

### 1. Correções Críticas (Integridade)
- **Alvo:** `OrdemServicoRequest`
- **Ação:** Tornar obrigatórios os campos de relacionamento.
  - Adicionar `@NotNull` em `clienteId` e `veiculoId`.
- **Justificativa:** Impedir a criação de ordens de serviço órfãs ou inválidas que causariam erros de execução (NPE) ou de banco de dados.

### 2. Correções de Segurança
- **Alvo:** `ResetPasswordRequest`
- **Ação:** Impor complexidade mínima de senha.
  - Adicionar `@Size(min = 8, max = 100)`.
  - (Futuro) Adicionar validador de complexidade (maiúsculas, minúsculas, números).
- **Alvo:** `TokenRequest`
- **Ação:** Restringir valores de `grant_type`.
  - Adicionar `@Pattern` para aceitar apenas os fluxos suportados (`password`, `refresh_token`, `authorization_code`).

### 3. Melhorias de Consistência (UX/DX)
- **Alvo:** `ClienteRequest`
- **Ação:** Refinar validação de documento.
  - Embora o VO valide o dígito verificador, o DTO deve validar a estrutura básica.
  - Implementar validação condicional ou genérica para diferenciar CPF de CNPJ na entrada, se possível, ou manter `@NotBlank` mas garantir feedback claro do VO.
- **Alvo:** Padronização de Mensagens
  - Adotar mensagens padrão do Bean Validation (`ValidationMessages.properties`) onde mensagens explícitas não forem estritamente necessárias, para facilitar internacionalização.

## Consequências

### Positivas
- **Robustez:** Redução drástica de erros de runtime (NPE) causados por dados de entrada incompletos.
- **Segurança:** Mitigação de riscos de senhas fracas e uso indevido de endpoints de autenticação.
- **Feedback:** Clientes da API receberão erros 400 mais precisos e rápidos.

### Negativas
- **Rigidez:** Clientes que hoje enviam requisições incompletas (que talvez funcionem por acidente) passarão a receber erros. Isso é considerado uma correção, não uma regressão.

## Status das Tarefas Relacionadas
As correções identificadas neste ADR derivam da análise documentada em `docs/validation_analysis.md` e devem ser tratadas como prioridade técnica no próximo ciclo de desenvolvimento.
