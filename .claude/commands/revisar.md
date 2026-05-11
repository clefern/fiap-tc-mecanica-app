# /revisar — Revisão Pós-Implementação

Após concluir uma implementação, execute este fluxo de revisão:

## Feature revisada

$ARGUMENTS

## 1. Tech Lead — revisão de estrutura e convenções

O Tech Lead verifica:
- Naming conventions seguidas (interfaces, impls, DTOs, mappers)
- Arquivos nos pacotes corretos
- Controller implementa a interface ControllerApi
- Insomnia atualizado com 2 entries (E2E numerado + Core folder)
- Sem imports desnecessários ou código morto

## 2. QA Engineer — revisão de testes

O QA verifica:
- Todos os cenários obrigatórios cobertos (happy path, 404, role inválida, 422)
- Assertions verificam campos do body (não só o status HTTP)
- Mocks configurados corretamente (`@MockBean`, exceções concretas)
- Nenhum cenário crítico ausente

## 3. Guardião do Domínio — verificação pós-implementação

O Guardião varre `domain/` buscando:
- Novas violações introduzidas (Spring/JPA annotations que escaparam)
- Regras de negócio que ficaram fora do agregado
- Value Objects sem validação no construtor

## 4. Output esperado

```
## Revisão: [nome da feature]

### Tech Lead
[resultado]

### QA Engineer
[resultado]

### Guardião do Domínio
[resultado]

## Resultado final
✅ Aprovado para merge  /  ⚠️ Ajustes necessários antes do merge
[lista de ações pendentes se houver]
```

## 5. Ações pendentes

Se houver problemas apontados, liste-os como checklist antes de marcar a feature como concluída.
