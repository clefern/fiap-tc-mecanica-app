# /planejar — Planejamento Arquitetural com Revisão por Pares

Antes de implementar qualquer feature, execute este fluxo:

## 1. Descreva o que será implementado

$ARGUMENTS

## 2. Acione os agentes de revisão

Execute os dois agentes de forma **sequencial**:

1. **Arquiteto Hexagonal** — valida a abordagem proposta:
   - Onde cada artefato deve viver (qual pacote, qual camada)
   - Se a porta necessária já existe ou precisa ser criada
   - Se o adapter correto será implementado
   - Referências aos ADRs relevantes

2. **Guardião do Domínio** — varre o código existente em `domain/` antes de planejar:
   - Identifica violações já presentes que o novo código não deve agravar
   - Confirma que os padrões corretos do domínio serão seguidos

## 3. Output esperado

Ao final, apresente um **plano de implementação** com:

```
## Artefatos a criar
- [ ] presentation/dto/XxxRequest.java
- [ ] presentation/dto/XxxResponse.java
- [ ] presentation/api/XxxControllerApi.java  (interface OpenAPI)
- [ ] presentation/controller/XxxController.java
- [ ] presentation/mapper/XxxMapper.java  (se necessário)
- [ ] application/service/XxxService.java  (se necessário)
- [ ] application/service/impl/XxxServiceImpl.java  (se necessário)
- [ ] domain/repository/XxxRepository.java  (se nova porta)
- [ ] infra/adapter/JpaXxxRepositoryAdapter.java  (se nova porta)
- [ ] src/test/.../XxxControllerTest.java

## Artefatos a modificar
- [ ] SecurityConfig.java  (se nova rota ou filtro)
- [ ] application.yml  (se nova property)
- [ ] Insomnia_export.yaml  (2 entries obrigatórios)

## Riscos e decisões
- [listar decisões que precisam ser tomadas]

## Aprovação do Arquiteto
[resultado do agente Arquiteto Hexagonal]

## Resultado do Guardião
[resultado do agente Guardião do Domínio]
```

## 4. Aguarde aprovação antes de implementar

Não inicie a implementação sem confirmação explícita do usuário.
