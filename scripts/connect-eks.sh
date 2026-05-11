#!/usr/bin/env bash
set -euox pipefail

# connect-eks.sh — Conecta kubectl local ao cluster EKS na AWS
# Uso:
#   ./scripts/connect-eks.sh                    # Usa defaults (lab, us-east-1, lab-fiap-cluster)
#   ./scripts/connect-eks.sh prod               # Conecta ao ambiente prod
#   ./scripts/connect-eks.sh lab us-west-2      # Sobrescreve região

ENV="${1:-lab}"
REGION="${2:-us-east-1}"
CLUSTER_NAME="${3:-${ENV}-fiap-cluster}"

log() {
  echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] $*"
}

log "🔗 Conectando ao cluster EKS..."
log "  Ambiente: ${ENV}"
log "  Região: ${REGION}"
log "  Cluster: ${CLUSTER_NAME}"
echo ""

# Verifica se AWS CLI está disponível
if ! command -v aws &> /dev/null; then
  echo "❌ AWS CLI não encontrado. Instale com: https://aws.amazon.com/cli/"
  exit 1
fi

# Verifica se kubectl está disponível
if ! command -v kubectl &> /dev/null; then
  echo "❌ kubectl não encontrado. Instale com: https://kubernetes.io/docs/tasks/tools/"
  exit 1
fi

# Verifica credenciais AWS
log "🔐 Verificando credenciais AWS..."
if ! aws sts get-caller-identity &> /dev/null; then
  echo "❌ Credenciais AWS não configuradas. Necessária configuração manual."
  exit 1
fi

ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
log "✅ Credenciais ok (Account: ${ACCOUNT_ID})"
echo ""

# Atualiza kubeconfig local
log "📥 Atualizando kubeconfig local..."
aws eks update-kubeconfig --region "${REGION}" --name "${CLUSTER_NAME}"
echo ""

# Testa conexão
log "✅ Conexão configurada!"
echo ""
log "📋 Informações do cluster:"
kubectl cluster-info
echo ""

# Mostra nodes
log "🖥️  Nodes:"
kubectl get nodes
echo ""

# Lista namespaces
log "📦 Namespaces:"
kubectl get namespaces | grep -E "NAME|mecanica|default"
echo ""

# Verifica se namespace do ambiente existe
NAMESPACE="mecanica-${ENV}"
if kubectl get namespace "${NAMESPACE}" &> /dev/null; then
  log "🎯 Recursos no namespace ${NAMESPACE}:"
  kubectl get all -n "${NAMESPACE}"
  echo ""
  
  # Verifica se há pods com problemas
  FAILED_PODS=$(kubectl get pods -n "${NAMESPACE}" --field-selector=status.phase!=Running,status.phase!=Succeeded 2>/dev/null | tail -n +2 | wc -l || echo "0")
  FAILED_PODS=$(echo "$FAILED_PODS" | tr -d '[:space:]')
  if [[ "${FAILED_PODS}" -gt 0 ]]; then
    log "⚠️  Atenção: ${FAILED_PODS} pod(s) com problemas detectado(s)"
    kubectl get pods -n "${NAMESPACE}" --field-selector=status.phase!=Running,status.phase!=Succeeded
    echo ""
  fi
else
  log "⚠️  Namespace ${NAMESPACE} não encontrado"
  echo ""
fi

log "✅ Pronto! Agora você pode usar kubectl normalmente."
echo ""
echo "💡 Comandos úteis:"
echo "   kubectl get pods -n ${NAMESPACE}"
echo "   kubectl logs -f -n ${NAMESPACE} deployment/lab-mecanica-app"
echo "   kubectl describe pod -n ${NAMESPACE} <pod-name>"
echo "   kubectl exec -it -n ${NAMESPACE} deployment/lab-postgres -- psql -U mecanica_user -d mecanica"
echo "   kubectl port-forward -n ${NAMESPACE} service/lab-mecanica-service 8080:80"
