#!/usr/bin/env bash
set -euox pipefail

# ---------------------------------------------------------------------------
# k8s-deploy.sh — Aplica o overlay kustomize para o ambiente informado,
# substituindo variáveis de ambiente nos manifests via envsubst.
#
# Variáveis obrigatórias (devem vir do pipeline):
#   ENVIRONMENT            — nome do overlay (ex: develop, lab, prod)
#   AWS_ACCOUNT_ID         — ID da conta AWS
#   AWS_REGION             — região AWS (ex: us-east-1)
#   DB_HOST                — host do banco de dados
#   DB_USERNAME            — usuário do banco de dados
#   DB_PASSWORD            — senha do banco de dados
#   DB_NAME                — nome do banco de dados
# ---------------------------------------------------------------------------

# Validar todas as variáveis obrigatórias
REQUIRED_VARS=(
  ENVIRONMENT
  AWS_ACCOUNT_ID
  AWS_REGION
  DB_HOST
  DB_USERNAME
  DB_PASSWORD
  DB_NAME
)

# Variáveis opcionais (podem ser vazias ou ter default)
# MAIL_USERNAME
# MAIL_PASSWORD
ROLLOUT_TIMEOUT="${ROLLOUT_TIMEOUT:-900s}"
SKIP_ROLLOUT_CHECK="${SKIP_ROLLOUT_CHECK:-false}"

log() {
  echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] $*"
}

fail() {
  log "❌ $*"
  exit 1
}

cleanup_tmp_dir() {
  if [[ -n "${TMP_BUILD_DIR:-}" && -d "${TMP_BUILD_DIR}" ]]; then
    log "🧹 Limpando diretório temporário: ${TMP_BUILD_DIR}"
    rm -rf "${TMP_BUILD_DIR}"
  fi
}

trap cleanup_tmp_dir EXIT

log "🔑 Verificando variáveis de ambiente obrigatórias..."
for var in "${REQUIRED_VARS[@]}"; do
  if [[ -z "${!var:-}" ]]; then
    fail "Variável obrigatória não definida: ${var}"
  fi
done
log "✅ Todas as variáveis obrigatórias estão definidas."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}"/.. && pwd)"
BASE_SRC="${ROOT_DIR}/k8s/base"
OVERLAY_SRC="${ROOT_DIR}/k8s/overlays/${ENVIRONMENT}"

[[ -d "${BASE_SRC}" ]] || fail "Base Kustomize não encontrada: ${BASE_SRC}"
[[ -d "${OVERLAY_SRC}" ]] || fail "Overlay não encontrado para o ambiente '${ENVIRONMENT}': ${OVERLAY_SRC}"
[[ -f "${BASE_SRC}/kustomization.yaml" ]] || fail "kustomization.yaml não encontrado na base: ${BASE_SRC}"
[[ -f "${OVERLAY_SRC}/kustomization.yaml" ]] || fail "kustomization.yaml não encontrado no overlay: ${OVERLAY_SRC}"

log "🔎 Validando acesso ao cluster..."
kubectl cluster-info >/dev/null
kubectl auth can-i get pods >/dev/null || fail "Sem permissão mínima no cluster (get pods)."

TMP_BUILD_DIR="$(mktemp -d)"
TMP_K8S_DIR="${TMP_BUILD_DIR}/k8s"
TMP_BASE_DIR="${TMP_K8S_DIR}/base"
TMP_OVERLAY_DIR="${TMP_K8S_DIR}/overlays/${ENVIRONMENT}"
BUILT_MANIFESTS_FILE="${TMP_BUILD_DIR}/built-manifests.yaml"

log "📦 Copiando base e overlay para diretório temporário..."
mkdir -p "${TMP_K8S_DIR}/overlays"
cp -r "${BASE_SRC}" "${TMP_BASE_DIR}"
cp -r "${OVERLAY_SRC}" "${TMP_OVERLAY_DIR}"

log "⚙️ Substituindo variáveis de ambiente nos manifests..."
while IFS= read -r file; do
  log "  - Processando ${file}"
  envsubst < "${file}" > "${file}.tmp" && mv "${file}.tmp" "${file}"
done < <(find "${TMP_K8S_DIR}" -type f \( -name "*.yaml" -o -name "*.yml" \))

log "🔧 Construindo manifests finais com Kustomize..."
kustomize build "${TMP_OVERLAY_DIR}" > "${BUILT_MANIFESTS_FILE}"
[[ -s "${BUILT_MANIFESTS_FILE}" ]] || fail "Manifestos gerados estão vazios."

log "🚀 Aplicando manifests no cluster..."
kubectl apply -f "${BUILT_MANIFESTS_FILE}"

if [[ "${SKIP_ROLLOUT_CHECK}" != "true" ]]; then
  log "⏳ Validando rollout (timeout: ${ROLLOUT_TIMEOUT})..."
  RESOURCE_NAMES="$(kubectl get -f "${BUILT_MANIFESTS_FILE}" -o name 2>/dev/null || true)"

  DEPLOYMENTS="$(printf '%s\n' "${RESOURCE_NAMES}" | grep '^deployment\.apps/' || true)"
  STATEFULSETS="$(printf '%s\n' "${RESOURCE_NAMES}" | grep '^statefulset\.apps/' || true)"

  if [[ -z "${DEPLOYMENTS}" && -z "${STATEFULSETS}" ]]; then
    log "ℹ️ Nenhum Deployment/StatefulSet encontrado para validar rollout."
  else
    while IFS= read -r resource; do
      [[ -z "${resource}" ]] && continue
      log "  - Rollout status: ${resource}"
      kubectl rollout status "${resource}" -n "mecanica-${ENVIRONMENT}" --timeout="${ROLLOUT_TIMEOUT}"
    done <<< "$(printf '%s\n%s\n' "${DEPLOYMENTS}" "${STATEFULSETS}")"
  fi
else
  log "⚠️ SKIP_ROLLOUT_CHECK=true, validação de rollout ignorada."
fi

log "✅ Deploy concluído para o ambiente: ${ENVIRONMENT}"
