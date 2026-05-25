# ADR-034 — Observabilidade via OpenTelemetry Collector + New Relic

**Status:** Accepted
**Data:** 2026-05-22
**Fase do projeto:** Fase 3 — Tech Challenge (Grupo 14SOAT)
**Decisores:** Grupo 14SOAT
**Relacionado:** ADR-023 (monitoramento tempo médio serviços), ADR-024 (endpoint tempo médio OS), ADR-025 (arquitetura logging)

## Contexto

A Fase 3 do Tech Challenge exige integração com APM (Datadog OU New Relic), monitoramento de latência das APIs, recursos do K8s (CPU/memória), healthchecks, alertas para falhas de OS, logs JSON estruturados com correlação entre requisições, e dashboards (volume diário de OS, tempo médio por status, erros de integrações).

O grupo precisou escolher entre:
- **Datadog** — padrão de mercado, APM excelente, mas cobra por host (~$15/host/mês) com trial de 14 dias
- **New Relic** — free tier permanente de 100GB/mês ingest, suficiente pro escopo do Tech Challenge
- **Self-hosted (Prometheus + Grafana + Loki + Tempo)** — controle total, mas exige operar 4 stacks adicionais

## Decisão

Adotar **OpenTelemetry Collector** como camada de coleta in-cluster e **New Relic** como backend de APM.

### Topologia

```
                                ┌───────────────────────────────────────┐
                                │  New Relic (https://otlp.nr-data.net) │
                                │  • Traces, Metrics, Logs              │
                                │  • 2 dashboards + 3 alertas (IaC)     │
                                └───────────────▲───────────────────────┘
                                                │  OTLP HTTP
   ┌────────────────────────────┐     ┌─────────┴──────────┐
   │ Spring Boot app            │     │ OpenTelemetry      │
   │ (mecanica-app)             ├────►│ Collector          │
   │ • OTel Java Agent (jar)    │ OTLP│ (DaemonSet)        │
   │ • Micrometer OTLP exporter │     │ • hostmetrics      │
   │ • Logback JSON encoder     │     │ • kubeletstats     │
   │ • MonitoredOperationAspect │     │ • batch processor  │
   │ • CorrelationIdFilter      │     └────────────────────┘
   └────────────────────────────┘
```

### Componentes

**No app (`fiap-tc-mecanica-app`):**
- `pom.xml`: `opentelemetry-api 1.46.0` + `micrometer-registry-otlp` + `logstash-logback-encoder 9.0`
- `Dockerfile`: baixa `opentelemetry-javaagent v2.26.1` no build, JVM arg `-javaagent:/app/otel.jar`
- `logback-spring.xml`: appender JSON estruturado por profile (`!local` usa JSON com MDC; `local` usa console legível)
- `application.yml`: `management.otlp.metrics/tracing.endpoint = http://otel-collector:4318/...`
- `MonitoredOperationAspect` + `@MonitoredOperation`: 14+ métodos críticos instrumentados; métrica `mecanica.service.execution` com tags `status/operation/service/error_type`
- `CorrelationIdFilter`: MDC `correlationId` + header `X-Correlation-ID` propagado entre logs/traces
- Eventos de domínio (`OrdemServicoEmDiagnosticoEvent`, `EmExecucaoEvent`, `EntregueEvent`) + `OsHistoryListener` gravando duração entre transições em tabela `os_history` (migration `V17`)

**No infra-k8s (`fiap-tc-mecanica-infra-k8s`):**
- `k8s/base/otel-collector-daemonset.yaml` — DaemonSet `otel/opentelemetry-collector-contrib:0.118.0`
- `k8s/base/otel-collector-configmap.yaml` — pipeline:
  - receivers: `otlp` (gRPC 4317 + HTTP 4318) + `hostmetrics` (root_path /hostfs, 60s) + `kubeletstats`
  - processors: `batch` (10s timeout)
  - exporters: `debug` + `otlphttp/newrelic`
  - pipelines: traces, metrics, logs
- `k8s/base/otel-collector-rbac.yaml` — ServiceAccount + ClusterRole pra kubeletstats
- `infra/conf/17-observability.tf` — Terraform `newrelic_one_dashboard` (2 páginas) + `newrelic_alert_policy` (3 condições NRQL)

### Dashboards New Relic (como código em `17-observability.tf`)

**Página 1 — "Visão Geral de Negócio"**: OS criadas (volume), ops por status (Diagnóstico/Execução/Finalização), latência APIs, falhas de processamento, ciclo de vida.

**Página 2 — "Infraestrutura (OTel / Kubernetes)"**: CPU, memória, container metrics, JVM heap.

### Alertas (3 condições NRQL)

| Nome | Condição |
|---|---|
| `high_error_rate` | erro > 5% por 300s |
| `os_process_failure` | erros em `metricName LIKE 'os_%'` por 60s (at_least_once) |
| `app_downtime` | loss-of-signal > 5min |

## Alternativas consideradas

| Alternativa | Por que descartada |
|---|---|
| **Datadog** | Cobra por host (~$15/mês cada nó EKS); trial 14d insuficiente pra avaliar com escopo do Tech Challenge; menor flexibilidade com OTel |
| **New Relic com agente proprietário (não OTel)** | Lock-in; troca de vendor exige reescrever instrumentação. OTel + exporter é portável |
| **Self-hosted (Prometheus + Loki + Tempo + Grafana)** | Operar 4 stacks adicionais não cabe no escopo (Helm releases, scrape configs, retention, alerting); New Relic free tier resolve |
| **CloudWatch nativo** | Bom pra infra AWS, fraco pra APM L7 (traces/spans), sem dashboards de negócio fáceis |

## Consequências

- **Positivas:**
  - Single pane of glass (1 dashboard cobre app + infra)
  - Portabilidade: OTel exporter pode apontar pra Datadog/Honeycomb/Tempo sem mudar app
  - Auditoria de OS (eventos + duração) virou métrica de negócio facilmente
  - JSON logging com correlationId facilita debug cross-service
- **Negativas / dívidas:**
  - Free tier 100GB/mês — monitorar consumo (alertas no NR avisam quando > 80%)
  - Lock-in em New Relic só pra dashboards/alertas (queries NRQL não portam pra outros backends)
  - OTel Collector DaemonSet adiciona ~150MB por nó

## Implementação

Mergeada no commit `8af6394 feat: integrate OpenTelemetry Collector for observability` + `620eac8 feat: enhance OpenTelemetry Collector setup for improved observability` + `76878c2 dashboards newrelic, metrics+infra` (infra-k8s/main) e ramo `metrics` (app/main, commit `7141e17`).

## Referências

- [OpenTelemetry Collector Contrib](https://github.com/open-telemetry/opentelemetry-collector-contrib)
- [New Relic OTLP](https://docs.newrelic.com/docs/more-integrations/open-source-telemetry-integrations/opentelemetry/get-started/intro-to-otel/)
- ADR-023, ADR-024, ADR-025 (base de logging/métricas mantida)
