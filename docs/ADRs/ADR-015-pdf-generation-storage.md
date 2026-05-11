# ADR-015
: Estratégia de Geração e Armazenamento de PDF

## Status
Aceito

## Contexto
O sistema requer a geração de documentos PDF para "Orçamentos" para serem compartilhados com os clientes. Atualmente, não temos uma solução dedicada de armazenamento de objetos (como AWS S3) configurada na infraestrutura. Precisamos de uma maneira de fornecer esses PDFs aos usuários imediatamente, planejando uma solução futura escalável.

## Decisão
Implementaremos uma **Estratégia Híbrida/Evolutiva**:

1.  **Fase 1 (Atual - MVP): Geração Sob Demanda (Stateless)**
    - O sistema gerará o PDF em memória mediante solicitação.
    - O endpoint `GET /api/orcamentos/{id}/pdf` acionará a lógica de geração.
    - Os bytes do PDF serão transmitidos diretamente na resposta HTTP (`Content-Type: application/pdf`).
    - **Prós**: Sem dependências de armazenamento externo; implementação simples; dados sempre atualizados.
    - **Contras**: Uso intensivo de CPU se houver alto tráfego; sem persistência (o histórico pode mudar se os dados mudarem).

2.  **Fase 2 (Futuro): Armazenamento de Blob (Stateful)**
    - Após a geração, o PDF será enviado para um Object Storage (ex: AWS S3, MinIO).
    - A entidade `Orcamento` armazenará a `pdfUrl` ou `storageKey`.
    - A API retornará o arquivo armazenado ou uma URL pré-assinada.
    - **Prós**: Alivia a CPU; fornece histórico imutável; escalável.
    - **Contras**: Maior complexidade de infraestrutura e custo.

## Consequências
- **Design da API**: O contrato da API é projetado para retornar o conteúdo do arquivo diretamente. No futuro, isso pode mudar para um Redirecionamento 302 para a URL do S3, ou podemos manter o comportamento de proxy para ocultar a implementação do armazenamento.
- **Desempenho**: Devemos monitorar o uso da CPU durante a geração de PDFs.
- **Armazenamento**: O campo `urlPdf` na entidade `Orcamento` atualmente atua como um espaço reservado ou pode armazenar um caminho "virtual" até que a Fase 2 seja implementada.

## Conformidade
- Esta decisão adere ao objetivo do projeto de "Arquiteturas simples, mas robustas", evitando otimização prematura (configuração de mocks/containers S3) enquanto mantém o domínio limpo.
