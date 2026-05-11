# Artefatos de Arquitetura e DDD

Esta pasta contém os artefatos de design do domínio. Cada documento tem um foco específico e complementa os demais.

## Ordem de leitura sugerida

1. **[Linguagem Ubíqua](./linguagem_ubiqua.md)** — Glossário do domínio: entidades, value objects, enumerações, regras de negócio e máquina de estados da OS. **Referência canônica** — os demais documentos apontam para cá.

2. **[Event Storming](./event_storming.md)** — Fluxos temporais: eventos de domínio, comandos, políticas reativas, bounded contexts e hotspots. Inclui o mapeamento de bounded contexts para packages do código.

3. **[Diagramas C4](./diagramas.md)** — Visão estrutural: Context, Containers, Components (arquitetura hexagonal), diagramas de classe (Mermaid), diagramas de sequência e modelo ER do banco.

## Como eles se relacionam

```
Linguagem Ubíqua           Event Storming              Diagramas C4
(o quê: termos e regras)   (o quando: fluxos e eventos)  (o como: estrutura e código)
        │                          │                          │
        └──── referenciado por ────┴──── referenciado por ────┘
```

## Documentos relacionados

- [Arquitetura (visão geral)](../arquitetura.md) — resumo executivo que liga todos os artefatos
- [Decisões Arquiteturais (ADRs)](../decisoes.md) — o *porquê* de cada escolha técnica
