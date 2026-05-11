#!/bin/bash
set -euox pipefail

# Configurações iniciais
BUCKET_NAME="${1:-"fiap-tc-lab-tfstate"}"
REGION="${2:-"us-east-1"}"
PROFILE="${3:-"fiap-lab"}"
DYNAMO_TABLE="terraform-lock-table"

echo "🛠️  Iniciando Bootstrap de Infra de Estado (S3 + DynamoDB)..."

# 1. Garantir Bucket S3
echo "🔍 Verificando bucket S3: $BUCKET_NAME..."
if timeout 10 aws s3api head-bucket --bucket "$BUCKET_NAME" --profile "$PROFILE" 2>/dev/null; then
    echo "✅ Bucket $BUCKET_NAME já existe."
else
    echo "🪣 Criando bucket $BUCKET_NAME..."
    aws s3 mb "s3://$BUCKET_NAME" --region "$REGION" --profile "$PROFILE"
    
    echo "📋 Ativando versionamento..."
    aws s3api put-bucket-versioning \
      --bucket "$BUCKET_NAME" \
      --versioning-configuration Status=Enabled \
      --region "$REGION" \
      --profile "$PROFILE"
    echo "✅ Bucket configurado com sucesso."
fi

# 2. Garantir Tabela DynamoDB para Lock
echo "🔍 Verificando tabela DynamoDB: $DYNAMO_TABLE..."
if aws dynamodb describe-table --table-name "$DYNAMO_TABLE" --profile "$PROFILE" --region "$REGION" 2>/dev/null; then
    echo "✅ Tabela $DYNAMO_TABLE já existe."
else
    echo "⚡ Criando tabela de lock no DynamoDB..."
    aws dynamodb create-table \
        --table-name "$DYNAMO_TABLE" \
        --attribute-definitions AttributeName=LockID,AttributeType=S \
        --key-schema AttributeName=LockID,KeyType=HASH \
        --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
        --profile "$PROFILE" \
        --region "$REGION" > /dev/null
    
    echo "⏳ Aguardando tabela ficar ativa..."
    aws dynamodb wait table-exists --table-name "$DYNAMO_TABLE" --profile "$PROFILE" --region "$REGION"
    echo "✅ Tabela $DYNAMO_TABLE pronta para uso."
fi

echo "🚀 Ambiente de Estado Terraform pronto (S3 + DynamoDB)!"
