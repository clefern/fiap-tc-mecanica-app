#!/bin/sh
set -e

# Default values
DB_HOST=${DB_HOST:-postgres}
DB_PORT=${DB_PORT:-5432}
TIMEOUT=60

echo "⏳ Waiting for database at $DB_HOST:$DB_PORT..."

# Wait for the database to be ready
i=0
while ! nc -z "$DB_HOST" "$DB_PORT"; do
  i=$((i+1))
  if [ "$i" -ge "$TIMEOUT" ]; then
    echo "❌ Database connection timed out after $TIMEOUT seconds."
    exit 1
  fi
  echo "Still waiting for database... ($i/$TIMEOUT)"
  sleep 1
done

echo "✅ Database is ready!"

# Execute the command passed as arguments
exec "$@"
