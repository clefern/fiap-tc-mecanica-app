# Análise de Lacunas - Observabilidade Mecânica API

Após comparar as definições no arquivo `infra/conf/17-observability.tf` com os dados reais retornados pela API NerdGraph, identificamos métricas configuradas no dashboard que não estão versionadas no Terraform, além de sugestões para melhoria da visibilidade do negócio.

## 1. Métricas Presentes no Dashboard mas Faltantes no Terraform

### A. Latência Técnica por Operação (ms)
O dashboard atual possui um widget facetiado por `metric_operation`, útil para identificar qual método Java está lento. No Terraform, este widget foi substituído pela métrica de negócio.
**Sugestão:** Adicionar um novo widget de linha para latência técnica.
```hcl
    widget_line {
      title  = "Latência Técnica por Operação (ms)"
      row    = 13
      column = 1
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT average(os_cycle_time) FROM Metric WHERE service.name = 'mecanica' AND `metric_type` IS NULL FACET `metric_operation` SINCE 1 hour ago TIMESERIES"
      }
    }
```

## 2. Métricas de Negócio Identificadas (Dados Atuais)

Com base nos dados da API, observamos que o sistema está registrando exceções específicas de negócio que merecem visibilidade direta:

### B. Taxa de Violação de Prioridade
Identificamos 24 ocorrências de `ViolacaoPrioridadeException`. Isso indica tentativas de processar ordens fora da ordem da fila.
```hcl
    widget_billboard {
      title  = "Violações de Fila (Hoje)"
      row    = 13
      column = 7
      width  = 3
      height = 3

      nrql_query {
        query = "SELECT count(*) FROM Metric WHERE service.name = 'mecanica' AND error_type = 'ViolacaoPrioridadeException' SINCE today"
      }
    }
```

### C. Eficiência de Diagnóstico (Conversão)
Métrica para medir quantas OS saem de Diagnóstico e viram Orçamentos aprovados.
```hcl
    widget_pie {
      title  = "Conversão de Orçamentos"
      row    = 10
      column = 7
      width  = 6
      height = 3

      nrql_query {
        query = "SELECT count(*) FROM Metric WHERE service.name = 'mecanica' AND metricName IN ('os_approved', 'os_cancelled') SINCE today FACET metricName"
      }
    }
```

## 3. Ajustes de Filtro Recomendados
A API revelou que os status `success` e `error` estão "sujando" o gráfico de ciclo de vida.
**Recomendação:** Atualizar o `widget_bar` do Terraform para incluir o filtro `metric_type = 'business'`, que é mais resiliente do que filtrar por nomes de status.
