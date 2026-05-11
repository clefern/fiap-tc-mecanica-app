# ADR-002: Validation Architecture (Domain invariants + Bean Validation at the boundary)

## Context
We need a clear, scalable validation strategy that preserves domain integrity, avoids duplicated rules, and supports presentation-layer ergonomics (friendly error messages, DTO contracts). An early idea was to centralize all field validations into a separate generic class. This tends to become a low-cohesion, high-coupling anti-pattern and duplicates business rules.

## Decision
Adopt a layered validation approach:

1. Domain invariants in Value Objects and Entities (core domain)
   - Each Value Object (e.g., CPF, TelefoneBr, Email, PlacaVeiculo) enforces its own rules and normalization.
   - Entities validate mandatory associations and required attributes.
   - Violations throw domain exceptions (e.g., IllegalArgumentException/DomainException).

2. Bean Validation in the presentation layer (DTOs/controllers)
   - Use Jakarta Bean Validation annotations on request DTOs for presence (@NotNull/@NotBlank) and format (@CpfValid, @Telefone, etc.).
   - Custom validators (@CpfValid, @Telefone) must delegate to the corresponding Value Objects (VO.of(value)), never reimplement rules.
   - Keep Jakarta Validation out of the domain module to preserve Clean Architecture boundaries.

3. Cross-field and contextual rules in domain/application services
   - Rules requiring knowledge of multiple fields or external context are handled in services, not in field validators.

## Details
### Domain Layer (core)
- Guarantees correctness by construction: VO.of(value) either returns a normalized, valid instance or throws.
- No dependencies on Jakarta Validation.
- Normalization lives here (e.g., trim, case normalization, canonical formats).

### Presentation Layer (boundary)
- DTOs annotate fields with @NotNull/@NotBlank for presence and with @CpfValid/@Telefone for format/semantics.
- Validators return true for null to allow composition with @NotNull when presence is required.
- Map domain exceptions to HTTP 400 with human-friendly messages.

### Cross-field Rules
- Implement in services with explicit, testable methods.
- Keep controllers thin; avoid complex validation logic in presentation.

### Null Handling
- Validators: null → true (presence controlled by @NotNull/@NotBlank).
- Domain: null for required attributes → throw IllegalArgumentException in constructors/factories.

### Error Mapping
- Presentation maps validation failures to 400 responses.
- Domain exceptions are translated at the boundary with consistent error bodies.

### Testing & TDD
- Write tests for VOs first (Red-Green-Refactor), then for validators that delegate to VOs.
- Add controller integration tests asserting 400 for invalid DTOs and success for valid cases.

## Alternatives Considered
- Single centralized validator class for all fields: rejected due to poor cohesion, rule duplication, and maintenance overhead.

## Consequences
- Pros: high cohesion, single source of truth (VOs), cleaner controllers, easier maintenance.
- Cons: requires clear boundaries and some boilerplate (DTO annotations + custom validators), but improves scalability.

## References
- /Users/cleberfernandes/Workspace/Personal/fiap-arquitetura-soft/techChallenge/mecanica/src/main/java/com/fiap/mecanica/presentation/validation/CpfValid.java
- /Users/cleberfernandes/Workspace/Personal/fiap-arquitetura-soft/techChallenge/mecanica/src/main/java/com/fiap/mecanica/presentation/validation/CPFValidator.java
- /Users/cleberfernandes/Workspace/Personal/fiap-arquitetura-soft/techChallenge/mecanica/src/main/java/com/fiap/mecanica/presentation/validation/Telefone.java
- /Users/cleberfernandes/Workspace/Personal/fiap-arquitetura-soft/techChallenge/mecanica/src/main/java/com/fiap/mecanica/presentation/validation/TelefoneValidator.java
