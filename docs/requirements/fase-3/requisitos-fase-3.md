# Requisitos — Fase 3 (13SOAT Tech Challenge)

> Fonte oficial: `13SOAT - Fase 3 - Tech Challenge.pdf` (mesmo diretório)
> Mecânica API — Grupo 14SOAT

---

## Contexto

Com a expansão da oficina para múltiplas unidades e o aumento constante na base de clientes, tornou-se necessário garantir **segurança, escalabilidade e alta disponibilidade**, além de obter **visibilidade total** sobre o funcionamento do sistema.

A direção da oficina quer:

- Controlar acessos e autenticações com segurança;
- Monitorar o ambiente e detectar gargalos em tempo real;
- Adotar soluções **serverless** para autenticação e notificações;
- Segregar a aplicação em repositórios organizados com CI/CD completo;
- Melhorar e documentar a modelagem do banco de dados, garantindo consistência e performance.

## Objetivo

Elevar a aplicação a um nível de **operação corporativa**, utilizando práticas de cloud, infraestrutura como código, segurança e observabilidade.

---

## Requisitos obrigatórios

### 1. Autenticação e API Gateway

- [ ] Implementar um **API Gateway** (AWS API Gateway, Kong, Traefik ou outro)
- [ ] Proteger rotas sensíveis da aplicação com autenticação via **CPF**
- [ ] Criar uma **Function Serverless** que:
  - [ ] Valida o CPF do cliente
  - [ ] Consulta a existência e o status do cliente na base de dados
  - [ ] Gera e devolve um token (JWT) válido para consumo das APIs protegidas

### 2. Estrutura de Repositórios e CI/CD

Organizar o projeto em **quatro repositórios separados**, cada um com CI/CD implementado (GitHub Actions, GitLab CI, etc.) e deploy automático para a nuvem:

- [ ] Repo 1 — **Lambda** (Function Serverless)
- [ ] Repo 2 — **Infraestrutura Kubernetes** (Terraform)
- [ ] Repo 3 — **Infraestrutura do Banco de Dados Gerenciado** (Terraform)
- [ ] Repo 4 — **Aplicação principal** executando em Kubernetes

Regras de proteção:

- [ ] Branch `main`/`master` protegida (sem commits diretos)
- [ ] Uso obrigatório de Pull Requests para merge
- [ ] Deploy automático das branches de homologação e produção

### 3. Infraestrutura obrigatória (cloud livre)

- [ ] **API Gateway** para controle e roteamento
- [ ] **Function Serverless** para autenticação
- [ ] **Banco de Dados Gerenciado** (PostgreSQL, MySQL, SQL Server, etc.)
- [ ] **Cluster Kubernetes** com escalabilidade
- [ ] **Terraform** para provisionamento

### 4. Monitoramento e Observabilidade

- [ ] Implementar integração com ferramentas como **Datadog** ou **New Relic** (escolha livre)
- [ ] Monitorar:
  - [ ] Latência das APIs
  - [ ] Consumo de recursos do Kubernetes (CPU, memória)
  - [ ] Healthchecks e uptime
  - [ ] Alertas para falhas no processamento de ordens de serviço
  - [ ] Logs estruturados (JSON), incluindo correlação entre requisições
- [ ] Expor dashboards com:
  - [ ] Volume diário de ordens de serviço
  - [ ] Tempo médio de execução por status (Diagnóstico, Execução, Finalização)
  - [ ] Erros e falhas nas integrações

### 5. Documentação da Arquitetura

- [ ] **Diagrama de Componentes** (visão de nuvem, APIs, banco e monitoramento)
- [ ] **Diagrama de Sequência** para os fluxos de autenticação e abertura de OS
- [ ] **RFCs** (Request for Comments) para decisões técnicas relevantes (ex.: escolha da nuvem, do banco, da estratégia de autenticação)
- [ ] **ADRs** (Architecture Decision Records) para decisões arquiteturais permanentes (ex.: padrão de comunicação, uso de HPA)
- [ ] Justificativa formal para a escolha do banco + diagramas ER + explicação dos relacionamentos

---

## Entregáveis

### Repositórios Git

- [ ] 4 repositórios separados com código, CI/CD e instruções claras no `README.md`
- [ ] Todos os repositórios devem incluir:
  - [ ] Dockerfiles (quando aplicável)
  - [ ] Pipelines de CI/CD funcionais
  - [ ] Links para os deploys ativos (se aplicável)

### README.md (em cada repositório)

- [ ] Descrição clara do propósito
- [ ] Tecnologias utilizadas
- [ ] Passos para execução e deploy
- [ ] Diagrama da arquitetura específica daquele repositório
- [ ] Link para o Swagger/Postman das APIs

### Vídeo de demonstração

- [ ] Upload no YouTube ou Vimeo (público ou não listado), até **15 minutos**
- [ ] Demonstrar:
  - [ ] Autenticação com CPF
  - [ ] Execução da pipeline CI/CD
  - [ ] Deploy automatizado
  - [ ] Consumo das APIs protegidas
  - [ ] Dashboard de monitoramento com análise ao vivo
  - [ ] Logs e traces em execução

### Entrega no Portal do Aluno

- [ ] Documento PDF único contendo:
  - [ ] Links dos 4 repositórios
  - [ ] Link do vídeo (até 15 minutos)
  - [ ] Links das documentações
  - [ ] Confirmação do usuário `soat-architecture` adicionado a todos os repositórios

---

> **Próximo passo**: ver `roadmap-fase-3.md` para o status consolidado de cada item (✅ concluído, ⚠️ parcial, ❌ pendente) e o gap em relação ao código atual.
