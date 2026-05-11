# ADR-011: Reestruturação da Gestão Inteligente de Estoque

## Status

Aceito

## 1. Contexto e Problema

### Cenário Atual

A gestão de estoque da oficina opera atualmente de forma reativa e manual. A verificação de disponibilidade é feita apenas no momento da criação da Ordem de Serviço (OS), e a reposição depende da percepção visual dos mecânicos ou almoxarifes.

### Desafios Específicos

- **Rupturas de Estoque (Stockouts):** Peças críticas faltam no momento da manutenção, causando bloqueio de OSs e atrasos na entrega do veículo.
- **Excesso de Estoque (Overstock):** Capital imobilizado em itens de baixo giro (ex: peças específicas de modelos antigos) que ocupam espaço e depreciam.
- **Processo de Reposição Lento:** O ciclo entre a detecção da falta e a chegada do item é longo e burocrático.

### Métricas de Desempenho Atual (Estimadas)

- **Nível de Serviço (Service Level):** ~75% (1 em cada 4 OSs sofre atraso por peça).
- **Rupturas:** Frequentes em itens de Curva C (baixo valor, alta frequência, ex: fixadores).
- **Acuracidade de Inventário:** Baixa, devido à falta de baixa automática em tempo real.

## 2. Solução Proposta

Implementaremos um **Sistema de Gestão de Estoque Híbrido (Reativo + Preditivo)** integrado ao fluxo da OS.

### Estratégia de Reposição

Adotaremos o modelo de **Revisão Contínua (Min/Max)**:

- Cada SKU (Stock Keeping Unit) terá parâmetros dinâmicos de `Estoque Mínimo` (Ponto de Pedido) e `Estoque Máximo` (Capacidade/Alvo).

### Sistema de Alertas Multinível

1.  **Pré-Alerta (Aviso):** Quando `Estoque Atual <= (Mínimo + 20%)`.
    - Ação: Notificação informativa para Planejamento.
2.  **Estoque Crítico (Urgente):** Quando `Estoque Atual <= Mínimo`.
    - Ação: Disparo imediato de fluxo de reposição.
3.  **Ruptura (Bloqueio):** Quando `Estoque Atual = 0`.
    - Ação: Bloqueio de novas OSs que dependam do item; alerta de alta prioridade.

### Mecanismo de Reposição Automática

Para itens classificados como **Estratégicos (Curva A)** e **Alto Giro**, o sistema gerará `SolicitacoesCompra` automaticamente.

- **Gatilho:** Atingimento do Ponto de Pedido.
- **Quantidade:** `Qtd = Estoque Máximo - Estoque Atual`.

### Fluxo de Aprovação

- **Automático:** Pedidos < R$ 500,00 E Itens Padronizados.
- **Manual (Gerente):** Pedidos > R$ 500,00 OU Itens Especiais/Sob Encomenda.

## 3. Planejamento de Reposição

### Definição de Parâmetros (Categorização)

Utilizaremos a matriz **ABC/XYZ** para definir políticas:

| Categoria | Descrição                        | Política de Estoque       | Estoque de Segurança          |
| :-------- | :------------------------------- | :------------------------ | :---------------------------- |
| **AX**    | Alto Valor / Demanda Constante   | Just-in-Time (JIT)        | Baixo (1-2 dias)              |
| **AY**    | Alto Valor / Demanda Variável    | Revisão Periódica         | Médio                         |
| **CX**    | Baixo Valor / Demanda Constante  | Estoque Elevado (Bulk)    | Alto (evitar stockout barato) |
| **CZ**    | Baixo Valor / Demanda Esporádica | Sob Demanda (não estocar) | Zero                          |

### Modelos de Previsão

- **Itens Regulares:** Média Móvel Simples (últimos 3 meses).
- **Itens Sazonais (ex: Baterias no Inverno):** Média Móvel Ponderada com fator de sazonalidade.

## 4. Sistema de Notificações

### Stakeholders e Canais

1.  **Almoxarife (Operacional):**
    - Canal: Painel de Controle (Dashboard) e Push Notification (App).
    - Eventos: Separação de peças, Recebimento de material.
2.  **Comprador (Tático):**
    - Canal: E-mail diário (Relatório de Reposição) e Sistema ERP.
    - Eventos: Aprovação de pedidos, Falhas de fornecedor.
3.  **Gerente de Oficina (Estratégico):**
    - Canal: Relatório Semanal e Alertas Críticos (SMS/WhatsApp Business API).
    - Eventos: Rupturas críticas, Aprovação de pedidos de alto valor.

### Templates de Mensagem

- **Alerta de Reposição:**
  > "️ **Atenção Estoque**: O item `[COD-123] Óleo 5W30` atingiu o nível mínimo (10 un). Uma solicitação de compra automática #9988 foi gerada."
- **Alerta de Ruptura:**
  > " **RUPTURA DETECTADA**: Não há estoque para `[COD-456] Pastilha Freio`. 3 OSs impactadas. Ação imediata necessária."

## 5. Considerações Técnicas

### Integração

- **ERP/WMS:** A solução deve expor webhooks (`POST /webhooks/stock-alert`) para integração com sistemas de gestão de armazém legados ou futuros.
- **Fornecedores:** Futura integração EDI/API para envio direto de pedidos aos parceiros principais.

### Requisitos de Dados

- Histórico de movimentação de estoque de no mínimo 12 meses para calibração precisa dos modelos de previsão.
- Cadastro de `Lead Time` (tempo de entrega) por fornecedor atualizado.

### Machine Learning (Opcional/Futuro)

- Implementação de modelos (Prophet/ARIMA) para refinar o `Ponto de Pedido` baseando-se em tendências de mercado e sazonalidade climática.

## 6. Critérios de Sucesso

1.  **Redução de Rupturas:** < 2% de OSs paradas por falta de peça em 6 meses.
2.  **Nível de Serviço:** Aumento para > 95% de disponibilidade imediata para itens Curva A.
3.  **Eficiência de Capital:** Redução de 15% no valor financeiro do estoque através da eliminação de obsoletos.
4.  **Automação:** 60% das reposições de itens de baixo valor (Curva C) realizadas sem intervenção humana.

## 7. Riscos e Mitigação

| Risco                                  | Probabilidade | Impacto | Mitigação                                                           |
| :------------------------------------- | :------------ | :------ | :------------------------------------------------------------------ |
| **"Efeito Chicote" (Bullwhip Effect)** | Média         | Alto    | Travas de segurança (teto máximo) para pedidos automáticos.         |
| **Falha na Integração de Fornecedor**  | Baixa         | Médio   | Fallback para envio de e-mail ao comprador interno se a API falhar. |
| **Dados Sujos (Estoque Fantasma)**     | Alta          | Alto    | Inventários cíclicos obrigatórios (contagem rotativa diária).       |
| **Erros de Previsão**                  | Média         | Médio   | Revisão mensal dos parâmetros de Mín/Máx (humano no loop).          |

## 8. Próximos Passos

1.  **Mapeamento de Portfólio:** Classificar todos os itens atuais na matriz ABC/XYZ.
2.  **Definição de Parâmetros:** Configurar `Lead Time`, `Qtd Mín` e `Qtd Máx` para os top 50 itens (Pareto).
3.  **MVP do Motor de Cálculo:** Implementar o listener de `BaixaEstoque` e a lógica de verificação de níveis.
4.  **Piloto:** Ativar reposição automática apenas para a categoria "Lubrificantes e Filtros".
5.  **Roll-out:** Expandir para demais categorias após 30 dias de validação.
