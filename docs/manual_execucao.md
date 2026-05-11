# Manual de Execução e Instalação

Este documento detalha todos os passos necessários para configurar, executar e validar o projeto **Mecânica API**, tanto para avaliação quanto para desenvolvimento.

---

## Guia Rápido (Quick Start)

Se você tem o **Docker** instalado, basta um comando para rodar tudo:

```bash
make dev
```

Este comando irá construir a aplicação, subir o banco de dados e iniciar a API.

Acesse a API em: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Pré-requisitos

### Para Execução (Avaliação)

- **Docker Desktop** (ou Docker Engine + Compose)
- **Git**

### Para Desenvolvimento

- **Java JDK 21+** (opcional se usar apenas Docker)
- **Maven 3.9+** (opcional, se não quiser usar o wrapper via Docker)
- **IDE** (IntelliJ IDEA recomendado)

---

## Fluxo Completo de Avaliação (Passo a Passo)

Este fluxo foi pensado para o avaliador conseguir rodar o projeto do zero, apenas com Docker e Git.

1. Clonar o repositório

   ```bash
   git clone <URL_DO_REPOSITORIO>
   cd techChallenge/mecanica
   ```

2. Garantir que está na branch correta

   ```bash
   git checkout main
   git pull origin main
   ```

3. Subir o ambiente completo com Docker

   ```bash
   make dev
   ```

   Isso irá: construir a aplicação, subir o PostgreSQL e iniciar a API.

   Endpoints principais:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

4. Gerar o Maven Wrapper (caso ainda não exista `./mvnw`)

   O projeto usa **Maven Wrapper** (`./mvnw`) e não exige Maven instalado. Para gerar o wrapper usando apenas Docker:

   ```bash
   chmod +x mvn-docker.sh
   ./mvn-docker.sh -N org.apache.maven.plugins:maven-wrapper-plugin:3.2.0:wrapper
   ```

   Isso criará: `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.jar` e `maven-wrapper.properties`.

   Caso tenha Maven instalado:
   ```bash
   mvn -N org.apache.maven.plugins:maven-wrapper-plugin:3.2.0:wrapper
   ```

5. Rodar a suíte de testes

   ```bash
   ./mvnw test
   # ou
   make test
   ```

6. (Opcional) Testes de integração

   ```bash
   ./mvnw verify -Pintegration-tests
   # ou
   make integration-test
   ```

7. Finalizar a avaliação

   ```bash
   make clean-docker
   ```

---

## Comandos de Execução (Makefile)

| Comando | Descrição |
|---------|-----------|
| `make dev` | **(Recomendado)** Inicia a aplicação com rebuild completo |
| `make run` | Roda a aplicação localmente (requer Java instalado) |
| `make test` | Executa a suíte de testes unitários |
| `make integration-test` | Executa testes de integração |
| `make clean-docker` | **Reset Total**: Remove containers, volumes e recria o banco |
| `make logs` | Acompanha os logs da aplicação em tempo real |
| `make format` | Formata o código automaticamente (Spotless) |
| `make coverage` | Gera o relatório de cobertura de testes (JaCoCo) |
| `make allure-serve` | Abre o relatório visual de testes (Allure) |

---

## Execução via Docker (Detalhada)

```bash
docker compose up --build -d    # subir o ambiente
docker compose logs -f app      # verificar logs
docker compose down             # parar a execução
```

### Serviços Disponíveis

Ver tabela completa de serviços e portas em [Infraestrutura](./infraestrutura.md#desenvolvimento-local-docker-compose).

---

## Análise de Qualidade (SonarQube)

1. **Iniciar infraestrutura:**
   ```bash
   make sonar-up
   ```

2. **Configuração inicial (primeiro acesso):**
   - Acesse http://localhost:9000
   - Login padrão: `admin` / `admin`
   - O sistema solicitará a troca de senha (sugestão: `sonar`)

3. **Gerar token de segurança:**
   - Vá em **My Account** > **Security**
   - Gere um novo token (ex: `my-local-token`)
   - Cada desenvolvedor deve gerar seu próprio token

4. **Executar análise:**

   Com token (mais seguro):
   ```bash
   export SONAR_TOKEN=seu_token_aqui
   make sonar
   ```

   Sem token (usando senha):
   ```bash
   export SONAR_PASSWORD=sua_nova_senha
   make sonar
   ```

5. **Visualizar relatório:** atualize a página do SonarQube após a execução.

6. **Parar infraestrutura:**
   ```bash
   make sonar-down
   ```

---

## Usuários de Teste (Seeding)

O sistema inicia com dados pré-carregados. Ver tabela completa de usuários no [README do projeto](../README.md#quick-start).

---

## Sistema de Emails (Simulação)

O projeto não envia emails reais. Todos os emails (notificações de orçamento, status de OS) são interceptados pelo **MailHog**.

Acesse http://localhost:8025 para visualizar os emails enviados pelo sistema.

---

**Grupo 14SOAT — FIAP 2025/2026**
