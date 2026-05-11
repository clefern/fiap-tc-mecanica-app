# ADR-006: Unificação de Estratégia de Validação (Reafirmação do Modelo de Delegação)

## Contexto
Durante a revisão de débitos técnicos (PR-5), foi levantada a questão sobre a necessidade de manter validações na camada de apresentação (pacote `validation`), dado que os *Value Objects* (VOs) na camada de domínio já implementam regras de integridade.

A preocupação central é:
1.  **Redundância:** Estamos duplicando lógica?
2.  **Responsabilidade:** Quem deve validar o dado?
3.  **Complexidade:** Vale a pena manter validadores customizados (`@CpfValid`, etc.) apenas para chamar o domínio?

Atualmente, o projeto segue o padrão definido no **ADR-002**, onde validadores de apresentação delegam a verificação para os VOs.

## Análise

### 1. Arquitetura Atual
- **Domínio (VOs):** Contém a "verdade" sobre as regras (invariantes). Ex: `CPF.java` contém o algoritmo de módulo 11. Se inválido, lança `IllegalArgumentException`.
- **Apresentação (DTOs):** Utiliza *Jakarta Bean Validation* (`@Valid`). Os validadores customizados (ex: `CPFValidator.java`) **não reimplementam** o algoritmo; eles instanciam o VO (`CPF.of(value)`) e capturam a exceção para retornar `false`.

### 2. Avaliação de Redundância
Não há redundância lógica (DRY - *Don't Repeat Yourself* é respeitado).
- O algoritmo matemático do CPF existe **apenas** em `CPF.java`.
- O `CPFValidator.java` atua como um **Adapter** (Padrão de Projeto) que traduz `IllegalArgumentException` (Domínio) para `ConstraintViolation` (Apresentação).

### 3. Princípios (SRP e Separação de Conceitos)
- **Value Objects:** Responsabilidade de **garantir integridade** do dado. Um objeto `CPF` nunca pode existir em estado inválido.
- **Camada de Apresentação:** Responsabilidade de **garantir contrato de interface** e feedback rápido ao cliente (UX/DX). Deve retornar erros estruturados (HTTP 400 com lista de campos) sem vazar *stack traces* ou exceções genéricas.

### 4. Impactos de Remover a Camada de Apresentação
Se removermos os validadores da apresentação (`@CpfValid`) e confiarmos apenas na exceção do VO durante a conversão DTO -> Domínio:
- **Pros:** Menos classes no pacote `validation`.
- **Cons:**
  - Perda do feedback automático e padronizado do Spring (`MethodArgumentNotValidException`).
  - Dificuldade em vincular o erro ao **campo específico** do JSON na resposta (ex: saber que foi o campo `cliente.documento` que falhou exigiria *parsing* manual da mensagem da exceção no `GlobalExceptionHandler`).
  - O fluxo de execução avançaria mais do que o necessário (chegaria até o Mapper ou Service antes de falhar), violando o princípio de *Fail Fast* na borda do sistema.

## Recomendação

A recomendação conjunta (Arquitetura & Operações) é **MANTER e REFORÇAR** a abordagem atual de delegação.

### Decisão
1.  **Unificação Lógica:** A lógica de validação deve permanecer **exclusiva** no Domínio (VOs).
2.  **Camada de Apresentação como Adapter:** Validadores customizados (`@Interface`) devem continuar existindo, mas **estritamente** delegando para o VO. É proibido duplicar regex ou lógica de negócios nos validadores.
3.  **Validações Estruturais:** Validações simples de presença (`@NotNull`, `@NotBlank`) e tamanho (`@Size`) devem permanecer nos DTOs, pois são contratos de entrada, não necessariamente regras de domínio complexas.

### Exemplo de Conformidade (Correto)
```java
// Apresentação
public class CPFValidator implements ConstraintValidator<CpfValid, String> {
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        try {
            CPF.of(value); // Delega para o domínio
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

### Exemplo de Violação (Incorreto)
```java
// Apresentação
public class CPFValidator ... {
    public boolean isValid(...) {
        // ERRADO: Reimplementar lógica de módulo 11 aqui
        if (value.length() != 11) return false;
        // ... calculo manual ...
    }
}
```

## Conclusão
A abordagem atual está alinhada com *Clean Architecture* e *Hexagonal Architecture*. Ela protege o domínio (invariantes) e, ao mesmo tempo, oferece uma API robusta e amigável (Bean Validation). A "sobrecarga" de criar classes de validação é compensada pela padronização das respostas de erro e pela garantia de que regras de domínio não "vazem" para a camada de apresentação na forma de código duplicado.
