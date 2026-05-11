#!/bin/bash

# Este script permite rodar comandos Maven dentro de um container Docker.
# Útil para quem NÃO quer instalar Java/Maven na máquina host.
# Uso: ./mvn-docker.sh [comandos maven]
# Exemplo: ./mvn-docker.sh clean install

# Verifica se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
  echo "Erro: Docker não está rodando. Inicie o Docker Desktop primeiro."
  exit 1
fi

# Roda um container Maven efêmero
# -v "$(pwd)":/usr/src/app  -> Monta o diretório atual no container
# -w /usr/src/app           -> Define o diretório de trabalho
# -v maven-repo:/root/.m2   -> Cria um volume para cachear dependências (não baixa tudo toda vez)
# --network host            -> Permite acessar o banco de dados rodando no localhost (se necessário)

echo "🐳 Rodando Maven via Docker..."

# Determine if TTY is available
if [ -t 1 ]; then
  TTY_ARGS="-it"
else
  TTY_ARGS="-i"
fi

docker run --rm $TTY_ARGS \
  -v "$(pwd)":/usr/src/app \
  -w /usr/src/app \
  -v mecanica-maven-repo:/root/.m2 \
  --network host \
  maven:3.9-eclipse-temurin-21-alpine \
  mvn "$@"
