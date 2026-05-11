# Documentacao — Mecanica API

Indice de toda a documentacao do projeto. Escolha o caminho conforme seu perfil.

---

## Por onde comecar

| Perfil | Caminho sugerido |
|--------|-----------------|
| **Avaliador FIAP** | [Manual de Execucao](./manual_execucao.md) → [README do projeto](../README.md) → [Entrega Fase 1](./entrega/fase-1/) / [Fase 2](./entrega/fase-2/) |
| **Desenvolvedor** | [Arquitetura](./arquitetura.md) → [Linguagem Ubiqua](./arquitetura/linguagem_ubiqua.md) → [Event Storming](./arquitetura/event_storming.md) |
| **DevOps** | [Infraestrutura](./infraestrutura.md) (Docker, K8s, Terraform, CI/CD) |
| **Arquiteto** | [Decisoes (ADRs)](./decisoes.md) → [Diagramas C4](./arquitetura/diagramas.md) → [Event Storming](./arquitetura/event_storming.md) |

---

## Documentos Principais

| Documento | Descricao |
|-----------|-----------|
| [Arquitetura](./arquitetura.md) | Hexagonal + DDD, modelo de dominio, padroes implementados, fluxo principal |
| [Infraestrutura](./infraestrutura.md) | Docker, Kubernetes (EKS), Terraform (AWS), CI/CD (GitHub Actions), variaveis de ambiente |
| [Qualidade e Testes](./qualidade.md) | Piramide de testes, cobertura JaCoCo, analise estatica, seguranca |
| [Decisoes Arquiteturais](./decisoes.md) | Indice dos 31 ADRs com links |
| [Manual de Execucao](./manual_execucao.md) | Quick start, fluxo de avaliacao, SonarQube, usuarios de teste |

---

## Artefatos DDD (`arquitetura/`)

| Documento | Descricao |
|-----------|-----------|
| [Linguagem Ubiqua](./arquitetura/linguagem_ubiqua.md) | Glossario completo dos termos do dominio — referencia canonica para entidades, VOs, enums e regras de negocio |
| [Event Storming](./arquitetura/event_storming.md) | Eventos, comandos, politicas, bounded contexts e hotspots |
| [Diagramas C4](./arquitetura/diagramas.md) | Context, Containers, Components, sequencias, ERD e infra AWS |

---

## Debitos Tecnicos (`debts/`)

| Documento | Descricao |
|-----------|-----------|
| [Debitos Tecnicos](./debts/debitos-tecnicos.md) | Lista consolidada e priorizada de melhorias, refactors e itens pendentes |

---

## Requisitos por Fase (`requirements/`)

| Documento | Descricao |
|-----------|-----------|
| [Requisitos Fase 1](./requirements/fase-1/roadmap-fase-1.md) | Requisitos oficiais e roadmap da Fase 1 (concluida) |
| [Requisitos Fase 2](./requirements/fase-2/requisitos-fase-2.md) | Requisitos oficiais da Fase 2 |
| [Roadmap Fase 2](./requirements/fase-2/roadmap-fase-2.md) | Roadmap de execucao da Fase 2 |

---

## Entrega (`entrega/`)

| Documento | Descricao |
|-----------|-----------|
| [Entrega Fase 1](./entrega/fase-1/) | PDF e script de geracao da Fase 1 |
| [Entrega Fase 2](./entrega/fase-2/) | PDF e script de geracao da Fase 2 |
| [Entrega Total](./entrega/entrega-total-grupo14soat.pdf) | PDF consolidado de ambas as fases |

---

## ADRs (`ADRs/`)

31 Architecture Decision Records documentando cada decisao tecnica relevante. Ver [indice completo](./decisoes.md).

---

## Recursos Externos

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html (requer app rodando) |
| Insomnia Collection | [`docs/api/Insomnia_export.yaml`](./api/Insomnia_export.yaml) |
| MailHog | http://localhost:8025 (dev) |
| Adminer | http://localhost:8081 (dev) |
| SonarQube | http://localhost:9000 (dev) |
