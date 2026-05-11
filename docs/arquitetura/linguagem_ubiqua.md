# Linguagem Ubíqua — Sistema Oficina Mecânica

> Glossário de termos acordados entre especialistas de domínio e o time de desenvolvimento.
> Todos os nomes de classes, métodos e campos no código seguem este vocabulário.

---

## Atores

| Termo | Papel no Sistema | Role |
|-------|-----------------|------|
| **Cliente** | Pessoa física (CPF) ou jurídica (CNPJ) proprietária do veículo. Aprova ou recusa orçamentos. | `CLIENTE` |
| **Atendente** | Funcionário de recepção. Abre OS, cadastra clientes e veículos. | `ATENDENTE` |
| **Mecânico** | Técnico responsável pelo diagnóstico e execução dos serviços. | `MECANICO` |
| **Admin** | Administrador do sistema. Gerencia usuários, catálogos e estoque. | `ADMIN` |

> **Nota:** Roles do `enum UserRole`: `ADMIN`, `ATENDENTE`, `MECANICO`, `CLIENTE`.

---

## Entidades e Agregados

### Ordem de Serviço (OS)
Aggregate root principal. Representa o ciclo completo de atendimento de um veículo.

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `id` | UUID | Identificador interno |
| `codigo` | String | Código legível (ex: `OS-20250315-143022-abc1`) |
| `status` | `StatusOS` | Estado atual na máquina de estados |
| `prioridade` | `Prioridade` | Nível de urgência operacional |
| `clienteId` | UUID | Referência ao cliente |
| `veiculoId` | UUID | Referência ao veículo |
| `mecanicoDiagnosticoId` | UUID | Mecânico que realizou o diagnóstico |
| `mecanicoExecucaoId` | UUID | Mecânico responsável pela execução |
| `valorTotal` | BigDecimal | Valor total calculado |
| `dataEntrada` | LocalDateTime | Quando a OS foi recebida |
| `dataAprovacao` | LocalDateTime | Quando o orçamento foi aprovado |
| `dataFechamento` | LocalDateTime | Quando a OS foi finalizada/entregue |
| `itens` | List\<ItemOrdemServico\> | Serviços, peças e insumos incluídos |

### Orçamento
Entidade vinculada a uma OS. Representa a estimativa de custo apresentada ao cliente.

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `id` | UUID | Identificador interno |
| `codigo` | String | Código legível |
| `ordemServicoId` | UUID | OS a qual pertence |
| `mecanicoDiagnosticoId` | UUID | Mecânico que emitiu o orçamento |
| `status` | `StatusOrcamento` | Estado atual |
| `valorTotalMateriais` | BigDecimal | Soma de peças e insumos |
| `valorTotalMaoDeObra` | BigDecimal | Soma dos serviços |
| `valorImpostos` | BigDecimal | Impostos calculados |
| `valorTotal` | BigDecimal | Total geral |
| `dataEmissao` | LocalDateTime | Data de geração |
| `dataValidade` | LocalDateTime | Data de expiração |
| `urlPdf` | String | Link para o PDF gerado |

### Item de Ordem de Serviço
Linha de detalhe de uma OS. Um único tipo representa serviços, peças e insumos.

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `tipo` | `TipoItem` | `SERVICO`, `PECA` ou `INSUMO` |
| `referenciaId` | UUID | ID do Serviço, Peça ou Insumo referenciado |
| `descricao` | String | Descrição do item |
| `quantidade` | Integer | Quantidade solicitada |
| `valorUnitario` | BigDecimal | Preço unitário no momento da inclusão |
| `subtotal` | BigDecimal | `valorUnitario × quantidade` |

### Itens do Catálogo (hierarquia)

```
ItemComercial (abstract)
├── Servico         — mão de obra; tem tempoEstimado e categoriaServico
└── ItemEstocavel (abstract)  — tem quantidade/estoqueMinimo/estoqueMaximo
    ├── Peca        — tem fabricante, codigoFabricante
    └── Insumo      — tem unidadeMedida (litro, metro, kg…)
```

### Veículo
| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `placa` | `PlacaVeiculo` | Placa brasileira (ABC-1234 ou Mercosul ABC1D23) |
| `marca`, `modelo` | String | Identificação do veículo |
| `ano` | int | Ano de fabricação (1900 – ano atual + 1) |

### Usuário (hierarquia)

```
User (abstract)  — id, nome, email, role, ativo
├── Cliente      — documento (CPF/CNPJ), tipo (FISICA/JURIDICA), telefone, endereco, veiculos
├── Atendente    — cpf
├── Mecanico     — cpf, especialidade
└── Admin        — (sem campos adicionais)
```

---

## Enumerações

### StatusOS — Máquina de estados da Ordem de Serviço

```
RECEBIDA
  └─► EM_DIAGNOSTICO
        └─► AGUARDANDO_APROVACAO
              ├─► EM_DIAGNOSTICO (rollback: orçamento rejeitado internamente)
              └─► APROVADA
                    └─► EM_EXECUCAO
                          └─► FINALIZADA
                                └─► ENTREGUE

Qualquer estado ──► CANCELADA
```

| Status | Quando ocorre |
|--------|--------------|
| `RECEBIDA` | OS aberta pelo atendente |
| `EM_DIAGNOSTICO` | Mecânico iniciou avaliação |
| `AGUARDANDO_APROVACAO` | Orçamento emitido e enviado ao cliente |
| `APROVADA` | Cliente aprovou o orçamento |
| `EM_EXECUCAO` | Mecânico iniciou execução dos serviços |
| `FINALIZADA` | Todos os serviços concluídos |
| `ENTREGUE` | Veículo devolvido ao cliente |
| `CANCELADA` | OS cancelada (qualquer estado anterior) |

### StatusOrcamento

| Status | Descrição |
|--------|-----------|
| `GERADO` | Orçamento criado, aguardando resposta |
| `APROVADO` | Cliente autorizou a execução |
| `REJEITADO` | Cliente recusou o orçamento |
| `EXPIRADO` | Prazo de validade vencido |
| `CANCELADO` | Cancelado por cancelamento da OS |

### Prioridade

| Valor | Peso | Significado |
|-------|------|-------------|
| `BAIXA` | 0 | Pode aguardar |
| `NORMAL` | 1 | Atendimento padrão |
| `ALTA` | 2 | Atenção especial |
| `URGENTE` | 3 | Prioridade máxima |

> A fila operacional ordena OS por peso decrescente de prioridade e data de entrada crescente (mais antiga primeiro dentro do mesmo nível).

### StatusEstoque

| Status | Condição |
|--------|----------|
| `NORMAL` | Quantidade acima do pré-alerta |
| `PRE_ALERTA` | Próximo ao mínimo (mín + 10% da faixa) |
| `CRITICO` | Quantidade ≤ estoqueMinimo |
| `RUPTURA` | Quantidade = 0 |

### CategoriaServico

`MANUTENCAO_PREVENTIVA` · `REPARO_MECANICO` · `ELETRICA` · `DIAGNOSTICO` · `ESTETICA` · `OUTROS`

### TipoItem

`SERVICO` · `PECA` · `INSUMO`

### TipoPessoa

`FISICA` (CPF) · `JURIDICA` (CNPJ)

---

## Value Objects

| VO | Validação |
|----|-----------|
| `Email` | Formato RFC 5322 |
| `CPF` | Algoritmo oficial + dígitos verificadores |
| `CNPJ` | Algoritmo oficial + dígitos verificadores |
| `Documento` | Abstração polimórfica de CPF ou CNPJ |
| `PlacaVeiculo` | Padrão brasileiro (ABC-1234) e Mercosul (ABC1D23) |
| `Endereco` | logradouro, número, bairro, cidade, estado, CEP |
| `TelefoneBr` | Formato brasileiro (11) 9xxxx-xxxx ou fixo |

---

## Regras de Negócio

### Transições de Status
- Toda transição inválida lança `TransicaoStatusInvalidaException` (422)
- A OS pode retornar de `AGUARDANDO_APROVACAO` para `EM_DIAGNOSTICO` apenas quando o orçamento é rejeitado internamente pelo mecânico (revisão de diagnóstico)
- O cancelamento é permitido a partir de qualquer estado (exceto `ENTREGUE`)

### Fluxo de Aprovação (ADR-022)
- O orçamento é gerado ao concluir o diagnóstico, não na abertura da OS
- A OS transita para `APROVADA` somente após o orçamento ser aprovado pelo cliente
- O estoque é baixado no momento da aprovação do orçamento (não na reserva), conforme ADR-019

### Aprovação pelo Cliente
O cliente pode aprovar ou recusar o orçamento de duas formas:
1. **Link tokenizado no email** — HMAC-SHA256 com expiração (via `IntegracaoOrcamentoController`)
2. **API M2M** — endpoint `POST /api/integracoes/orcamentos/aprovacao` com header `X-Api-Key`

### Estoque
- Estoque embutido nas entidades `Peca` e `Insumo` (campos: `quantidadeEstoque`, `estoqueMinimo`, `estoqueMaximo`)
- Baixa de estoque via `ItemEstocavel.baixarEstoque(quantidade)` — lança exceção se insuficiente
- Alerta de estoque baixo disparado por evento após operações de baixa

### Validações de Entrada
| Campo | Regra |
|-------|-------|
| CPF | Algoritmo oficial, único no sistema |
| CNPJ | Algoritmo oficial, único no sistema |
| Placa | Padrão brasileiro, única no sistema |
| Email | RFC 5322, único por usuário |
| Ano do veículo | Entre 1900 e ano atual + 1 |
| Prioridade | Deve ser um dos 4 valores do enum |

### Autorização
| Ação | Roles permitidos |
|------|-----------------|
| Criar OS / abertura completa | `ATENDENTE`, `ADMIN` |
| Transições de status da OS | `MECANICO`, `ATENDENTE`, `ADMIN` |
| Visualizar status da OS | Todos (incluindo `CLIENTE` — somente as próprias) |
| Aprovar/rejeitar orçamento (JWT) | `CLIENTE` (próprio), `ADMIN` |
| Aprovar/rejeitar orçamento (API Key) | Sistemas externos autorizados |
| Gerenciar catálogo (peças, serviços) | `ADMIN` |
| Gerenciar estoque | `ADMIN` |
| Alterar prioridade de OS | `ADMIN` |
| Relatórios | `ADMIN`, `ATENDENTE` |

---

## Métricas de Negócio Implementadas

| Métrica | Endpoint | Descrição |
|---------|----------|-----------|
| Tempo médio por mecânico | `GET /api/relatorios/tempo-medio` | Média de horas entre abertura e fechamento por mecânico |
| Fila operacional | `GET /api/ordens-servico/fila-operacional` | OS ativas ordenadas por prioridade + data |
| Status atual da OS | `GET /api/ordens-servico/{id}/status` | Estado atual + timestamps relevantes |
| Estoque abaixo do mínimo | `GET /api/estoque/alertas` | Peças e insumos com StatusEstoque ≠ NORMAL |
