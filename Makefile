# Makefile for Mecanica Project

.PHONY: help dev down clean logs test build package lint format run docs setup coverage integration-test docker-build clean-docker allure-serve allure-report site install-hooks quality-fix quality-check load-test sonar sonar-up sonar-down

help:
	@echo "🛠️  Mecanica Project Commands"
	@echo "============================"
	@echo "Docker Environment:"
	@echo "  make dev            - Start application with rebuild (Recommended)"
	@echo "  make down           - Stop application"
	@echo "  make clean-docker   - Stop application and remove volumes (Full Reset)"
	@echo "  make logs           - Follow application logs"
	@echo ""
	@echo "Maven / Development:"
	@echo "  make test           - Run unit tests"
	@echo "  make build          - Compile project"
	@echo "  make package        - Package application (JAR)"
	@echo "  make clean          - Clean build artifacts"
	@echo "  make lint           - Run static analysis (Checkstyle, PMD, SpotBugs)"
	@echo "  make format         - Auto-format code (Spotless)"
	@echo "  make run            - Run application locally"
	@echo "  make docs           - Generate Javadocs"
	@echo "  make site           - Generate comprehensive project reports"
	@echo ""
	@echo "Quality Scripts (Unified CLI):"
	@echo "  make install-hooks  - Install git hooks (pre-commit)"
	@echo "  make quality-fix    - Run auto-fix (formatting & imports)"
	@echo "  make quality-check  - Run quality checks with report generation"
	@echo ""
	@echo "Project Specific:"
	@echo "  make setup            - Setup development environment"
	@echo "  make coverage         - Generate coverage report (JaCoCo)"
	@echo "  make integration-test - Run integration tests"
	@echo "  make docker-build     - Build Docker image via Spring Boot"
	@echo "  make allure-serve     - Serve Allure report (Hot-reload)"
	@echo "  make allure-report    - Generate static Allure report"
	@echo "  make load-test        - Run basic load test scenario with k6"
	@echo "  make sonar-up         - Start SonarQube infrastructure"
	@echo "  make sonar            - Run SonarQube analysis (requires running container)"

# --- Docker Commands ---

dev:
	@echo "🚀 Starting application (forcing rebuild)..."
	docker compose up -d --build

rebuild:
	@echo "🔄 Rebuilding application keeping the data safe..."
	docker compose up -d --build --no-deps app

down:
	@echo "🛑 Stopping application..."
	docker compose down

clean-docker:
	@echo "🧹 Cleaning up containers and volumes..."
	docker compose down -v

logs:
	docker compose logs -f app

# --- Maven Commands ---

test: ## Executa os testes unitários usando o wrapper do Maven
	./mvnw test

## executa testes de um determinado arquivo:
  ## ./mvnw test -Dtest=AuthControllerTest

build: ## Compila o projeto sem executar os testes
	./mvnw compile

package: ## Empacota o projeto gerando o artefato final
	./mvnw package

clean: ## Limpa os arquivos gerados pelo build
	./mvnw clean

# Comandos de qualidade de código (Maven Direto)
lint: ## Executa análise estática de código (Checkstyle/PMD/SpotBugs)
	./mvnw checkstyle:check pmd:check spotbugs:check

format: ## Formata o código automaticamente seguindo os padrões do projeto (Spotless)
	./mvnw spotless:apply

site: ## Gera relatório completo do projeto (Maven Site)
	./mvnw site

# --- Quality Scripts (Unified CLI) ---

install-hooks: ## Instala hooks do git para garantir qualidade antes do commit
	./scripts/quality.sh install

quality-fix: ## Executa script de auto-fix (formatação e limpeza)
	./scripts/quality.sh fix

fix: quality-fix ## Alias curto para quality-fix

quality-check: ## Executa script de verificação de qualidade e gera relatórios
	./scripts/quality.sh check

# --- Comandos de desenvolvimento ---
sonar-up: ## Inicia infraestrutura do SonarQube (Profile 'audit')
	@echo "🚀 Starting SonarQube infrastructure..."
	docker compose --profile audit up -d sonarqube db

sonar-down: ## Para infraestrutura do SonarQube
	@echo "🛑 Stopping SonarQube infrastructure..."
	docker compose --profile audit stop sonarqube db

sonar: ## Executa análise do SonarQube (Requer container sonarqube rodando)
	@echo "🔍 Executing SonarQube Analysis..."
	@if [ -z "$(SONAR_TOKEN)" ]; then \
		echo "⚠️  SONAR_TOKEN not set. Using basic auth (admin/\$${SONAR_PASSWORD:-admin})."; \
		echo "ℹ️  If authentication fails, export SONAR_PASSWORD=your_new_password or generate a token."; \
		./mvnw clean verify sonar:sonar \
			-DskipITs \
			-Dsonar.projectKey=mecanicaapp \
			-Dsonar.host.url=http://localhost:9000 \
			-Dsonar.login=admin \
			-Dsonar.password=$${SONAR_PASSWORD:-admin} \
			-Dsonar.userHome=.sonar_local; \
	else \
		echo "🔐 Using provided SONAR_TOKEN."; \
		./mvnw clean verify sonar:sonar \
			-DskipITs \
			-Dsonar.projectKey=mecanicaapp \
			-Dsonar.host.url=http://localhost:9000 \
			-Dsonar.login=$(SONAR_TOKEN) \
			-Dsonar.userHome=.sonar_local; \
	fi


run: ## Executa a aplicação localmente
	./mvnw spring-boot:run

# --- Comandos de documentação ---

docs: ## Gera a documentação do projeto (Javadoc)
	./mvnw javadoc:javadoc

# Comandos específicos do projeto (extraídos da documentação em docs/*.md)
setup: install-hooks ## Configura o ambiente de desenvolvimento (Hooks + Dependências via Docker)
	./mvn-docker.sh clean install

coverage: ## Gera relatório de cobertura de testes
	./mvnw jacoco:report

integration-test: ## Executa testes de integração
	./mvnw verify -Pintegration-tests

docker-build: ## Constrói a imagem Docker da aplicação
	./mvnw spring-boot:build-image

# Comandos Allure (Testing Reports)
allure-serve: ## Gera relatório Allure e abre no navegador
	./mvnw allure:serve

allure-report: ## Gera relatório estático do Allure
	./mvnw allure:report

load-test:
	k6 run load-tests/fluxo-criacao-os-aprovacao.js
