#!/bin/bash
set -euox pipefail

BUCKET_NAME="${1:-"fiap-tc-lab-tfstate"}"
REGION="${2:-"us-east-1"}"
PROFILE="${3:-"fiap-lab"}"
DYNAMO_TABLE="terraform-lock-table"

# Evita travamento em ambientes com pager padrão da AWS CLI (less/more)
export AWS_PAGER=""

if ! command -v jq >/dev/null 2>&1; then
  echo "❌ Erro: jq não encontrado. Instale jq para executar o cleanup."
  exit 1
fi

delete_s3_items_by_query() {
  local query="$1"
  local item_type="$2"

  aws s3api list-object-versions --bucket "$BUCKET_NAME" --profile "$PROFILE" --region "$REGION" \
    --query "$query" --output json |
    jq -c '.[]?' | while IFS= read -r obj; do
      if [[ -n "$obj" ]]; then
        local key version_id
        key="$(echo "$obj" | jq -r '.Key')"
        version_id="$(echo "$obj" | jq -r '.VersionId')"
        echo "  - Deletando $item_type: $key (ID: $version_id)"
        aws s3api delete-object --bucket "$BUCKET_NAME" --key "$key" \
          --version-id "$version_id" --profile "$PROFILE" --region "$REGION"
      fi
    done
}

echo "🗑️  Iniciando Cleanup Profundo..."

# 1. Esvaziar e Deletar Bucket S3
echo "📦 Verificando bucket S3: $BUCKET_NAME..."
# Removido 2>/dev/null para mostrar erro se head-bucket falhar
if aws s3api head-bucket --bucket "$BUCKET_NAME" --profile "$PROFILE" --region "$REGION"; then
    echo "📦 Bucket encontrado: $BUCKET_NAME. Removendo versões e objetos..."
    
    # Remove versões de objetos
    echo "⏳ Listando e removendo versões de objetos..."
    delete_s3_items_by_query 'Versions[].{Key:Key,VersionId:VersionId}' "versão"

    # Remove delete markers
    echo "⏳ Listando e removendo delete markers..."
    delete_s3_items_by_query 'DeleteMarkers[].{Key:Key,VersionId:VersionId}' "delete marker"

    echo "🪣 Deletando bucket $BUCKET_NAME..."
    # O --force já é usado aqui para buckets não vazios
    aws s3 rb "s3://$BUCKET_NAME" --profile "$PROFILE" --region "$REGION" --force
else
    echo "ℹ️  Bucket $BUCKET_NAME não encontrado ou já deletado."
fi

# 2. Deletar Tabela DynamoDB de Lock
echo "🔐 Verificando tabela DynamoDB: $DYNAMO_TABLE..."
# Removido 2>/dev/null para mostrar erro se describe-table falhar
if aws dynamodb describe-table --table-name "$DYNAMO_TABLE" --profile "$PROFILE" --region "$REGION"; then
    echo "🔨 Deletando tabela de lock $DYNAMO_TABLE..."
    # Removido > /dev/null para mostrar saída/erro de delete-table
    aws dynamodb delete-table --table-name "$DYNAMO_TABLE" --profile "$PROFILE" --region "$REGION"
    echo "⏳ Aguardando remoção completa da tabela $DYNAMO_TABLE..."
    aws dynamodb wait table-not-exists --table-name "$DYNAMO_TABLE" --profile "$PROFILE" --region "$REGION"
    echo "✅ Tabela $DYNAMO_TABLE deletada."
else
    echo "ℹ️  Tabela $DYNAMO_TABLE não encontrada."
fi

echo "✨ Cleanup finalizado com sucesso!"
