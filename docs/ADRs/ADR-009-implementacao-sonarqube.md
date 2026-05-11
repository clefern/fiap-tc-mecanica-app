# ADR-009: Implementação de Análise Estática com SonarQube (Containerizado)

## Status
Aceito (Implementado com Melhorias de Segurança e Automação) — SEC-001 aplicado (2026-03-09)

## 1. Contexto

### Objetivos
O objetivo da implementação do SonarQube é estabelecer um mecanismo automatizado e contínuo de inspeção de código para detectar:
- Bugs e Code Smells.
- Vulnerabilidades de Segurança (SAST).
- Duplicação de código.
- Cobertura de testes unitários.

A meta é garantir que o código entregue siga os padrões de qualidade definidos (Clean Code, SOLID) e mantenha a dívida técnica controlada.

### Tecnologias e Ferramentas
- **SonarQube LTS Community**: Versão estável e gratuita para análise local.
- **Docker & Docker Compose**: Para orquestração da infraestrutura do SonarQube.
- **PostgreSQL 13**: Banco de dados dedicado para persistência das análises do SonarQube.
- **Maven Plugin (`sonar-maven-plugin`)**: Para execução da análise integrada ao build.
- **Makefile**: Automação dos comandos de infraestrutura e análise.

### Fluxo Atual (Melhorado)
A análise agora é integrada ao fluxo de desenvolvimento via `Makefile` e perfis Docker:
1. O desenvolvedor sobe a infraestrutura dedicada via `make sonar-up` (usa profile `audit`).
2. O desenvolvedor executa a análise via `make sonar`.
   - Se `SONAR_TOKEN` estiver definido, usa o token.
   - Caso contrário, usa credenciais padrão (`admin/sonar`).
3. O relatório é gerado e visualizado em `http://localhost:9000`.
4. Ao final, a infraestrutura pode ser parada com `make sonar-down`.

## 2. Decisão Atual

A solução adotada consiste em uma infraestrutura containerizada autocontida no projeto (`docker-compose.yml`), isolada da aplicação principal através de **Docker Profiles**.

### Detalhes Técnicos da Implementação
- **Serviço `sonarqube`**:
  - Imagem: `sonarqube:community`.
  - Porta: `9000`.
  - Profile: `audit` (não sobe por padrão com `make dev`).
  - Persistência: Volumes para dados (`sonar_data`) e logs (`sonar_logs`).
  - Rede: Isolada em `sonar_net`.
- **Serviço `db` (PostgreSQL)**:
  - Dedicado exclusivamente ao SonarQube.
  - Imagem: `postgres:13`.
  - Profile: `audit`.
  - Credenciais configuradas via variáveis de ambiente no compose.

### Integração
- **Automação via Makefile**: Comandos padronizados (`make sonar`, `make sonar-up`, `make sonar-down`).
- **Segurança**: Tokens não são mais hardcoded. O uso de variáveis de ambiente (`SONAR_TOKEN`) é encorajado.
- **Exclusões de Cobertura**: Configuradas no `pom.xml` para ignorar classes de configuração, DTOs e Mappers, focando a análise no domínio e regras de negócio.

## 3. Análise Crítica

### Pontos Fortes
- **Independência**: O ambiente de análise roda 100% local.
- **Custo Zero**: Utiliza a versão Community.
- **Isolamento**: Banco de dados separado.
- **Eficiência de Recursos**: O uso de profiles (`audit`) garante que o SonarQube só consuma recursos quando explicitamente solicitado.
- **Segurança Melhorada**: Remoção de tokens expostos e uso de credenciais gerenciadas.

### Limitações Restantes
- **Integração com CI Remoto**: A configuração atual é focada em execução local. Para CI/CD (GitHub Actions), seria necessário adaptar para usar SonarCloud ou uma instância exposta.
- **Performance Local**: A execução da análise ainda consome recursos da máquina do desenvolvedor, mas agora é sob demanda.

## 4. Recomendações Implementadas

### Segurança
- [x] **Revogar token exposto**: Token anterior invalidado.
- [x] **Remover arquivo inseguro**: `scripts/sonarQubeDocker.txt` removido.
- [x] **Variáveis de Ambiente**: Suporte a `SONAR_TOKEN` no Makefile.

### Otimização e Infraestrutura
- [x] **Makefile**: Comandos `make sonar`, `make sonar-up`, `make sonar-down` criados.
- [x] **Profiles do Docker**: Configurado profile `audit` para evitar inicialização desnecessária.

### Configuração
- [x] **Exclusões de Cobertura**: Adicionadas ao `pom.xml` (`sonar.exclusions`, `sonar.coverage.exclusions`).

## 5. Plano de Ação Futuro

1. **[Qualidade]** Resolver os "Code Smells" e "Bugs" apontados no relatório inicial (atualmente 173 Code Smells e 5 Bugs).
2. **[CI/CD]** Configurar workflow do GitHub Actions para rodar a análise em Pull Requests com secret `SONAR_TOKEN`. (Pendente — CICD-001)
3. **[Hardening]** Alterar a senha padrão `admin/sonar` para uma senha forte em ambiente produtivo (se aplicável).

## 6. Uso Correto

```bash
# Subir infraestrutura Sonar
make sonar-up

# Executar análise (com token — recomendado)
export SONAR_TOKEN=<token-gerado-em-localhost:9000>
make sonar

# Parar infraestrutura
make sonar-down
```

### Métricas de Sucesso Atuais
- Relatório gerado com sucesso em < 1 minuto.
- Zero configurações manuais complexas (apenas `make sonar-up` e `make sonar`).
- Segurança validada (sem credenciais no git).
