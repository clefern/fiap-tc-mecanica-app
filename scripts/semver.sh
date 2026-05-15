#!/usr/bin/env bash
set -euo pipefail

# semver.sh — Sistema de versionamento SemVer
# Uso:
#   ./scripts/semver.sh get-current-version    # Retorna versão do pom.xml
#   ./scripts/semver.sh get-latest-tag        # Retorna tag git mais recente
#   ./scripts/semver.sh get-next-version      # Calcula próxima versão (patch)
#   ./scripts/semver.sh update-version [VERSION] # Commit, tag e push da versão

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}"/.. && pwd)"
MVNW="${ROOT_DIR}/mvnw"

log() {
  echo -e "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] $*"
}

fail() {
  log "❌ $*"
  exit 1
}

get_current_version() {
  local version
  version=$($MVNW help:evaluate -Dexpression=project.version -q -DforceStdout)
  echo "$version"
}

get_latest_tag() {
  local tag
  tag=$(git -C "${ROOT_DIR}" tag --list --sort=-version:refname | grep -E '^[0-9]+\.[0-9]+\.[0-9]+$' | head -n1 || echo "0.0.0")
  echo "$tag"
}

get_next_version() {
  local current_version next_version
  current_version=$(get_current_version)

  IFS='.' read -ra VERSION_PARTS <<< "$current_version"

  local major="${VERSION_PARTS[0]}"
  local minor="${VERSION_PARTS[1]}"
  local patch="${VERSION_PARTS[2]}"

  next_version="$major.$minor.$((patch + 1))"
  echo "$next_version"
}

update_version() {
	local CHECK="$(git -C "${ROOT_DIR}" status --porcelain)"
	if [[ -n "$CHECK" ]]; then
		fail "Existem alterações locais não commitadas.\nPor favor, limpe o workspace antes de prosseguir.\n\n$CHECK"
	fi

	VERSION="${1:-}"
	if [[ -z "$VERSION" ]]; then
		VERSION="$(get_next_version)"
	fi

	log "Updating pom.xml to version $VERSION..."
	$MVNW versions:set -DnewVersion="$VERSION" -DgenerateBackupPoms=false -q

	git config --global user.name 'github-actions'
	git config --global user.email 'github-actions@noreply.com'

	git add pom.xml
	git commit -m "pipeline - bump to $VERSION (patch)"
	git tag -a "$VERSION" -m "Release $VERSION"

	local current_branch
	current_branch=$(git rev-parse --abbrev-ref HEAD)

	git push origin "$current_branch"
	git push origin "$VERSION"
}

case "${1:-}" in
  "get-current-version")
    get_current_version
    ;;
  "get-latest-tag")
    get_latest_tag
    ;;
  "get-next-version")
    get_next_version
    ;;
  "update-version")
  	update_version "${2:-}"
  	;;
  ""|"help"|"-h"|"--help")
    echo "Uso: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  get-current-version   Retorna versão do pom.xml"
    echo "  get-latest-tag        Retorna tag git mais recente"
    echo "  get-next-version      Calcula próxima versão (patch)"
    echo "  update-version [VER]  Atualiza pom.xml, faz commit, tag e push"
    echo "  help                  Mostra esta ajuda"
    ;;
  *)
    fail "Comando desconhecido: ${1:-}. Use --help para ajuda."
    ;;
esac
