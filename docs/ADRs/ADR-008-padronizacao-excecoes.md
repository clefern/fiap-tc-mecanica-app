# ADR-008: Padronização e Especialização da Hierarquia de Exceções

## Status
Proposto

## Contexto
Atualmente, o sistema `mecanica` utiliza uma mistura de exceções genéricas da Java Runtime (como `IllegalArgumentException`, `IllegalStateException`) e algumas exceções específicas (como `DuplicateDocumentoException`). 

Os problemas identificados com a abordagem atual são:
1. **Falta de Semântica**: Exceções como `IllegalArgumentException` não comunicam *qual* regra de negócio foi violada, apenas que um argumento estava errado.
2. **Dificuldade de Tratamento**: O `GlobalExceptionHandler` captura `IllegalArgumentException` e retorna 400 Bad Request para todos os casos, o que pode mascarar erros de lógica interna ou validações de domínio críticas.
3. **Observabilidade Limitada**: Logs mostram erros genéricos, dificultando a triagem de incidentes (diferenciar um erro de validação de input de um erro de estado de entidade).
4. **Acoplamento com Framework**: O uso direto de `EntityNotFoundException` (JPA) na camada de serviço vaza detalhes de implementação.

Precisamos de uma hierarquia de exceções robusta, orientada ao domínio (DDD), que permita tratamento granular e respostas de API consistentes.

## Decisão
Decidimos refatorar o sistema de tratamento de erros para adotar uma **Hierarquia de Exceções de Domínio Especializada**.

### 1. Regra de Ouro
**Nenhuma exceção genérica (RuntimeException, Exception, IllegalArgumentException, IllegalStateException) deve ser lançada diretamente pela camada de Domínio ou Aplicação para representar regras de negócio.**
Toda exceção lançada deve ser uma classe específica que herda da hierarquia do projeto.

### 2. Hierarquia Proposta

A hierarquia será baseada na natureza do erro (Negócio vs. Sistema) e no contexto (Domínio).

```mermaid
classDiagram
    class RuntimeException
    class MecanicaException {
        <<abstract>>
        +String code
        +String details
    }
    class BusinessException {
        <<abstract>>
        (HTTP 4xx)
    }
    class SystemException {
        <<abstract>>
        (HTTP 5xx)
    }
    
    RuntimeException <|-- MecanicaException
    MecanicaException <|-- BusinessException
    MecanicaException <|-- SystemException
    
    %% Ramos de Negócio (4xx)
    class ResourceNotFoundException {
        <<abstract>>
        (HTTP 404)
    }
    class DomainRuleException {
        <<abstract>>
        (HTTP 409/422)
    }
    class SecurityException {
        <<abstract>>
        (HTTP 401/403)
    }
    
    BusinessException <|-- ResourceNotFoundException
    BusinessException <|-- DomainRuleException
    BusinessException <|-- SecurityException
    
    %% Implementações Concretas (Exemplos)
    ResourceNotFoundException <|-- ClienteNaoEncontradoException
    ResourceNotFoundException <|-- VeiculoNaoEncontradoException
    ResourceNotFoundException <|-- OrdemServicoNaoEncontradaException
    
    DomainRuleException <|-- TransicaoStatusInvalidaException
    DomainRuleException <|-- EstoqueInsuficienteException
    DomainRuleException <|-- VeiculoJaCadastradoException
    
    %% Ramos de Sistema (5xx)
    SystemException <|-- DatabaseIntegrationException
    SystemException <|-- ExternalServiceException
```

### 3. Mapeamento de Exceções (AS-IS vs TO-BE)

| Contexto | Exceção Atual (Genérica) | Nova Exceção Especializada | Status HTTP |
|---|---|---|---|
| **Busca** | `EntityNotFoundException` | `[Entidade]NaoEncontradaException` | 404 |
| **Validação VO** | `IllegalArgumentException` | `[ValueObject]InvalidoException` | 400 |
| **Estado OS** | `IllegalStateException` | `StatusOrdemServicoInvalidoException` | 422 |
| **Duplicidade** | `DuplicateDocumentoException` | `DocumentoJaCadastradoException` | 409 |
| **Estoque** | (Inexistente) | `EstoqueInsuficienteException` | 422 |
| **Auth** | `AuthenticationException` | `CredenciaisInvalidasException` | 401 |

### 4. Especificação das Novas Exceções

#### 4.1. Base
Todas as exceções devem estender `MecanicaException` e fornecer um código de erro único para rastreabilidade.

```java
public abstract class MecanicaException extends RuntimeException {
    private final String code;
    
    protected MecanicaException(String message, String code) {
        super(message);
        this.code = code;
    }
}
```

#### 4.2. Exceções de Domínio (BusinessException)

**Categoria: Entidade Não Encontrada (404)**
- **Nome**: `OrdemServicoNaoEncontradaException`
- **Contexto**: Tentativa de operar sobre uma OS inexistente.
- **Mensagem**: "Ordem de Serviço não encontrada com ID: {id}"
- **Código**: `OS-404`

**Categoria: Regra de Negócio / Estado (422/409)**
- **Nome**: `TransicaoStatusInvalidaException`
- **Contexto**: Tentar mover OS de `CANCELADA` para `EM_EXECUCAO`.
- **Mensagem**: "Não é possível transitar OS de {statusAtual} para {novoStatus}"
- **Código**: `OS-422-01`

- **Nome**: `EstoqueInsuficienteException`
- **Contexto**: Adicionar peça em OS sem saldo no estoque.
- **Mensagem**: "Estoque insuficiente para a peça {pecaId}. Solicitado: {qtd}, Disponível: {saldo}"
- **Código**: `STK-422-01`

### 5. Fluxo de Tratamento (GlobalExceptionHandler)

O `GlobalExceptionHandler` será refatorado para:
1. Capturar `BusinessException`: Retornar status dinâmico baseada na sub-classe (404, 409, 422) e corpo padronizado (Problem Details RFC 7807).
2. Capturar `SystemException`: Logar com nível ERROR (com stacktrace) e retornar 500 com mensagem genérica "Erro interno, contate suporte" (ocultando detalhes internos).
3. Capturar `RuntimeException` (Genérica): Logar como CRITICAL (pois é um erro não mapeado/bug) e retornar 500.

### 6. Benefícios (Consequências)
- **Positivo**: API auto-documentável pelos códigos de erro.
- **Positivo**: Frontend pode reagir a códigos específicos (`STK-422-01` -> Oferecer produto similar) em vez de apenas exibir "Erro".
- **Positivo**: Logs limpos (Warnings para erros de negócio, Errors para falhas de sistema).
- **Negativo**: Maior verbosidade e número de classes no projeto (trade-off aceito pela clareza).

## Plano de Ação
1. Criar pacote `com.fiap.mecanica.domain.exception`.
2. Implementar classes base (`MecanicaException`, `BusinessException`, `SystemException`).
3. Refatorar `OrdemServico.java` para usar `TransicaoStatusInvalidaException` em vez de `IllegalStateException`.
4. Refatorar Services para usar `[Entidade]NaoEncontradaException`.
5. Atualizar `GlobalExceptionHandler`.

---
**Autores**: Melissa (Arquitetura) & Lucas (Implementação)
**Data**: 2026-01-10
