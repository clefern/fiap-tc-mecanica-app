<!--
  Project Rules & Preferences
  Maintainer: Team Mecanica
  Last Updated: 2025-11-21
-->

# Project Rules & Preferences (Enterprise-Grade Development)

## Development Philosophy — Three-Persona Collaboration

- @TDD: Test-driven development with Red-Green-Refactor.
- @Mentor: Code review culture, patterns, best practices, accessibility.
- @Architect: System design, performance, scalability, security.
- Workflow: Maintain internal dialogue between personas for comprehensive decisions.

## Core Development Principles

- TDD-first with meaningful assertions and 90%+ coverage target.
- Quality gates: pre-commit hooks (format, lint, tests as appropriate).
- Performance focus at every layer (build, runtime, DB, API).
- Accessibility first (WCAG 2.2) in UI components.
- Scalable architecture (Feature-sliced, Clean Architecture, DDD alignment).

## Technology & Code Quality

- TypeScript strict mode where applicable; Java domain remains type-safe.
- Linting with strict rules; automated formatting via Prettier/formatters.
- Git hooks enforce quality automation.
- Testing: unit, integration (Testcontainers), e2e when UI is present.

## Communication & Response Style (for team processes)

- Context restoration on session start; check rules and history.
- Structured responses: clear headings and code blocks.
- Practical focus over theory; mention performance implications.
- Code changes: explanation first, minimal patches, include absolute paths.
- Validation of changes: run tests and lint; avoid fabricating non-existent code.

## Architecture & Design Preferences

- Feature-sliced design: src/features, src/shared.
- Atomic design for UI components.
- Clean Architecture: domain isolated; adapters in infra; presentation separate.
- DDD boundaries for business logic.

## Component Standards (when UI exists)

- Documentation, Storybook, accessibility, keyboard navigation.
- Performance optimized (lazy loading, code splitting).
- Test-driven components.

## Project Structure Template

```
src/
├── features/
│   └── [feature-name]/
│       ├── components/
│       ├── __tests__/
│       └── index.ts
├── shared/
│   ├── types/
│   └── ui/
├── App.*
├── main.*
└── setupTests.*
```

## Documentation Standards

- **Centralized Documentation**: All project documentation (ADRs, Plans, Requirements) is located in the `docs/` directory.
- **Naming Convention**: Use kebab-case for filenames (e.g., `notification-system-plan.md`, `api-authentication-flow.md`).
- **Comprehensive READMEs**: tech stack, architecture, setup.
- **Performance metrics**: document build times and optimizations.
- **ADRs** for major decisions (e.g., ADR-002 Validation Architecture).
  - **Language**: All ADRs MUST be written in Portuguese.
- **Endpoint Documentation**: All communication-related endpoints must follow the standard template in `docs/templates/endpoint-template.md`.
- **API Response Documentation**:
  - **MANDATORY**: All controller methods must include `@ApiResponses` annotations.
  - **Centralization Strategy**:
    - A global handler (`OpenApiConfig`) defines default responses (400, 401, 403, 500) for all endpoints.
    - Controllers can override or add specific responses (e.g., 200, 201, 404, 409) using `@ApiResponse` annotations.
    - If no specific annotation is provided, the global defaults still apply.
    - Always verify consistency with existing HTTP status codes used in the application.
- **Insomnia Export**:
  - **MANDATORY**: The `docs/api/insomnia_export.json` file MUST contain all API endpoints available in the application.
  - Any new controller or endpoint added to the code MUST be immediately reflected in this export file to ensure the collection is always complete and testable.
- **Component docs**: stories and examples.
- **Session context** in this file to ensure continuity.

## Quality Standards

- Type safety, 90%+ coverage, WCAG 2.2 accessibility, Core Web Vitals.
- Security: CSP headers and best practices.
- Dev experience: fast feedback, HMR, instant type checking.
- Automated quality: pre-commit hooks; warning tolerance set appropriately.

## Performance Benchmarks (Targets)

- Build: 40%+ faster with modern tooling.
- Linting speed: 60%+ faster with optimized configs.
- HMR: 10x faster than legacy bundlers.
- Bundle size: 50%+ smaller with ESM tree-shaking.

## Development Workflow

1. Context restoration at session start.
2. TDD-first implementation.
3. Three-persona review chain (@TDD → @Mentor → @Architect).
4. **MANDATORY**: Implement unit tests for every new line of code.
5. **MANDATORY**: Update seeders/factories to be consistent with any entity changes.
6. **MANDATORY**: Data seeding MUST be done via Java Seeders/Factories, NOT via SQL migration files.
7. **MANDATORY**: Keep `docs/api/insomnia_export.json` updated with any API changes.
8. Quality validation before commit.
9. Real-time documentation updates.
10. **MANDATORY**: Always verify available commands in the `Makefile` before executing scripts.
11. **MANDATORY**: Generate an ADR (Architectural Decision Record) whenever planning the next implementation phase, ensuring high-level architectural alignment before coding.

## Scripts (template)

```bash
make dev            # Start application with rebuild
make test           # Run unit tests
make lint           # Run static analysis
make format         # Auto-format code
make build          # Compile project
make clean-docker   # Reset environment
```

## Immediate Opportunities

1. Component Library and documentation.
2. E2E testing framework.
3. Performance monitoring and budgets.
4. Visual regression testing.

## Advanced Features

1. State management.
2. API/server state management.
3. Internationalization.
4. Security headers and practices.

## Team Collaboration

1. Documentation expansion with examples.
2. ADRs for architectural alignment.
3. CI/CD with automated testing/deploy.
4. Monitoring and error tracking.

## 💡 **Key Technical Decisions & Learnings**

### **Established Best Practices**

- **ESLint v9 Migration**: Flat config for future compatibility
- **ES Modules**: Explicit module type for performance and clarity
- **Warning Tolerance**: Set to 5 warnings for development flexibility
- **Feature-Sliced Design**: Adopted for scalable architecture
- **Exception Logging**: MANDATORY use of `❌` prefix in all exception logs for visibility, logger.error and logger warn.
- **Maven Wrapper**: Maven is not installed locally. ALWAYS use the wrapper `./mvnw` for executing Maven commands.
- **Import Organization**: All imports MUST be grouped at the top of the file. Do not disperse imports within the code.
- **Test Coverage**: A coverage verification script is available at `check_coverage.py`. Use it to verify coverage for specific tests or modules.

### **Performance Optimizations**

## Validation Architecture (Summary)

- Invariants in domain (VOs, entities) with normalization; domain throws on violation.
- Bean Validation at presentation boundary (@CPF, @Telefone) delegating to VOs.
- Cross-field rules in services.
- See ADR-002:
  /Users/cleberfernandes/Workspace/Personal/fiap-arquitetura-soft/techChallenge/mecanica/docs/ADRs/ADR-002-validation-architecture.md

## Exception Handling (Standardization)

- **Granularity**: All exceptions MUST be granular and specific (e.g., `OrdemServicoNaoEncontradaException` instead of `EntityNotFoundException`).
- **Hierarchy**: Follow the `MecanicaException` -> `BusinessException` / `SystemException` hierarchy.
- **Logging**: Implement appropriate logging for each exception type (Error for System, Info/Warn for Business).
- **Messages**: Maintain clear and useful error messages for diagnosis.
- **Documentation**: Document custom exceptions in the project documentation (ADRs).
- See ADR-008:
  /Users/cleberfernandes/Workspace/Personal/fiap-arquitetura-soft/techChallenge/mecanica/docs/ADRs/ADR-008-padronizacao-excecoes.md
