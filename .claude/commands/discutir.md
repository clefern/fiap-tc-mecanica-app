# /discutir — Debate Arquitetural entre Personas

Aciona um debate estruturado entre o Arquiteto Hexagonal e o Tech Lead sobre uma decisão de design.

## Tema do debate

$ARGUMENTS

---

## Fluxo do debate

### Rodada 1 — Proposta inicial

**Arquiteto Hexagonal** analisa o tema sob a ótica de:
- Aderência à arquitetura hexagonal (Ports & Adapters)
- Coerência com ADRs existentes em `docs/ADRs/`
- Impacto na pureza do domínio
- Onde cada artefato deve viver

### Rodada 2 — Revisão técnica

**Tech Lead** responde com foco em:
- Viabilidade de implementação (convenções do projeto, padrões existentes)
- Riscos práticos (complexidade, tempo, manutenibilidade)
- Pontos de discordância com a proposta do Arquiteto
- Referências a código existente similar no projeto

### Rodada 3 — Síntese

Com base nas duas perspectivas, apresente:

```
## Decisão Recomendada

### Abordagem escolhida
[descrição da abordagem vencedora]

### Justificativa
[por que esta abordagem foi preferida]

### Pontos de atenção
[riscos ou trade-offs que o time deve conhecer]

### Artefatos impactados
[lista de arquivos/pacotes que serão afetados]

### ADR necessário?
[ ] Sim — esta decisão merece um novo ADR em docs/ADRs/
[ ] Não — está coberta por ADRs existentes (ADR-XXX)
```

---

## Exemplos de uso

```
/discutir Usar CQRS para separar leituras da fila operacional
/discutir Implementar cache em OrdemServicoService.listar()
/discutir Autenticação do endpoint de integração: API key vs OAuth2 client credentials
/discutir Publicar evento de domínio vs chamar service diretamente após aprovação de orçamento
```

---

## Regras do debate

- Cada persona defende sua perspectiva com argumentos técnicos baseados no código real do projeto
- Referências a arquivos concretos têm peso maior que argumentos abstratos
- A síntese deve ser acionável — não "depende", mas uma recomendação clara
- Se não houver consenso, apontar explicitamente o trade-off para o usuário decidir
