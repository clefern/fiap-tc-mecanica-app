# /api-dod — Definition of Done para Endpoints

Checklist completo de Definition of Done para um novo endpoint da Mecânica API.

## Endpoint

$ARGUMENTS

---

## Checklist de DoD

### Código

- [ ] **Interface ControllerApi** definida em `presentation/api/` com anotações OpenAPI (`@Operation`, `@ApiResponse`)
- [ ] **Controller** implementa a interface, anotado com `@RestController`, `@RequestMapping`, `@PreAuthorize`
- [ ] **DTO Request** em `presentation/dto/` com validações Bean Validation (`@NotNull`, `@NotBlank`, etc.)
- [ ] **DTO Response** em `presentation/dto/` com `@Builder` e campos apropriados
- [ ] **Mapper** em `presentation/mapper/` converte domain model ↔ DTO (sem lógica de negócio)
- [ ] **Service** (se novo serviço): interface em `application/service/`, impl em `application/service/impl/`
- [ ] **Porta de repositório** (se novo acesso a dados): interface em `domain/repository/`
- [ ] **Adapter** (se nova porta): `Jpa*RepositoryAdapter.java` em `infra/adapter/` implementa a porta

### Domínio puro

- [ ] Nenhuma anotação Spring ou JPA em `domain/`
- [ ] Regra de negócio implementada no agregado (domain model), não no service
- [ ] Exceção de domínio usa subclasse concreta de `DomainRuleException` ou `ResourceNotFoundException`

### Testes

- [ ] Teste de controller (`@SpringBootTest + @AutoConfigureMockMvc + @ActiveProfiles("test")`)
- [ ] Happy path com assertions no body (id, campos críticos)
- [ ] 404 quando recurso não encontrado
- [ ] 403/401 para role sem acesso (se rota tem `@PreAuthorize`)
- [ ] 422 para regra de domínio violada (se aplicável)
- [ ] 400 para entrada inválida (se há Bean Validation no request)

### Segurança

- [ ] `@PreAuthorize` define as roles permitidas
- [ ] Endpoint listado em `SecurityConfig` na chain correta (JWT ou API key)
- [ ] Rota pública (se for o caso) adicionada à lista de `permitAll()` no SecurityConfig

### Documentação e integração

- [ ] **Insomnia — E2E folder**: entry numerado `"NN. Role - Descrição"` com response chaining
- [ ] **Insomnia — Core folder**: entry sem número `"Verbo + Recurso"` com `{{ access_token }}`
- [ ] Swagger documenta o endpoint (verificar em `/swagger-ui.html`)

### Formatação e qualidade

- [ ] `./mvnw spotless:apply` executado (ou pre-commit hook passou)
- [ ] `./mvnw test` — todos os testes passando
- [ ] Sem imports não usados

### Branch e commit

- [ ] Branch: `feat/<nome-da-feature>`
- [ ] Commit segue padrão: `feat(<escopo>): <descrição> (<ID>)`
  - Ex: `feat(ordens-servico): add status endpoint (API-003)`

---

## Validação pelos agentes

Execute `/revisar <feature>` para confirmação automatizada de:
- Tech Lead (estrutura + Insomnia)
- QA Engineer (cobertura de testes)
- Guardião do Domínio (pureza do domain/)
