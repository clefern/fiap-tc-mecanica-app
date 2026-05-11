# ADR-028: Reestruturação da Documentação para Entrega Final

## Status

Aceito

## Contexto

O projeto está em fase final de preparação para entrega do Tech Challenge (Pós-graduação). A documentação atual cresceu organicamente e contém arquivos redundantes, detalhes de implementação obsoletos e informações dispersas. Para facilitar a avaliação pelos professores e garantir que todos os entregáveis estejam claros, é necessária uma reorganização completa.

## Decisão

Decidimos reestruturar a documentação focando na clareza de navegação e na relevância para a avaliação acadêmica.

A nova estrutura será centralizada no `README.md`, que atuará como um "Hub", delegando os detalhes para documentos específicos na pasta `docs/`.

### 1. Estrutura de Arquivos

- **`README.md` (Raiz)**: Visão geral, links rápidos, resumo da arquitetura e guia de início rápido (1 comando).
- **`docs/manual_execucao.md`**: Detalhes completos de setup, Docker, Makefile, requisitos de sistema.
- **`docs/arquitetura_ddd.md`**: Consolidação dos artefatos de DDD (Event Storming, Linguagem Ubíqua, Diagramas C4/Classes).
- **`docs/api_reference.md`**: Detalhes sobre a API, Swagger, Insomnia e padrões de resposta.
- **`docs/qualidade_testes.md`**: Estratégia de testes, cobertura, relatórios de vulnerabilidade e ferramentas de qualidade.
- **`docs/decisoes_tecnicas.md`**: Índice das ADRs e justificativas macro (Banco de dados, Monolito, etc.).
- **`docs/A_next_technical_debts.md`**: (Mantido) Lista de débitos técnicos e próximos passos.

### 2. Arquivos a Remover/Arquivar

- `docs/CODE_REVIEW_REPORT.md`: Irrelevante para avaliação final.
- `docs/LINTING.md`: Detalhe de desenvolvimento, movido para `manual_execucao.md` se necessário.
- `docs/SETUP.md`: Conteúdo movido para `manual_execucao.md`.
- `docs/OPTIMIZATION_PLAN.md`: Conteúdo relevante movido para `A_next_technical_debts.md` ou descartado.
- `docs/ROADMAP.md`: Conteúdo relevante movido para `A_next_technical_debts.md`.
- `docs/REQUIREMENTS.md`: Renomeado para `docs/escopo_projeto.md` para referência.

### 3. Critérios de Qualidade

- Todos os links no `README.md` devem funcionar.
- Comandos de execução devem ser testados e validados.
- Informações sobre "Monolito" e "Arquitetura Hexagonal" devem estar explícitas em `arquitetura_ddd.md`.

## Consequências

- **Positivo**: Documentação mais limpa e focada na avaliação.
- **Positivo**: Facilidade para encontrar os entregáveis obrigatórios (Vídeo, DDD, Código, Relatórios).
- **Negativo**: Perda de histórico de discussões menores (aceitável para entrega final).
