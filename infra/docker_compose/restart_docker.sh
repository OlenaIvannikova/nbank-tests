#!/bin/bash

# Режимы безопасности Bash
# set -euo pipefail
  # Это 3 режима безопасности Bash:
  # -e	Остановить скрипт при ошибке команды
  # -u	Ошибка при использовании неопределённой переменной
  # -o pipefail	Ошибка в pipeline, если завершилась ошибкой любая команда
set -euo pipefail

# Определяем корневую директорию проекта
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd -- "$SCRIPT_DIR/../.." && pwd)"

# Пути к конфигурационным файлам
# Путь к Docker Compose
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

# Путь к browsers.json
json_file="$PROJECT_DIR/selenoid/config/browsers.json"

# Проверяем обязательные переменные окружения
: "${POSTGRES_DB:?POSTGRES_DB is not set}"
: "${POSTGRES_USER:?POSTGRES_USER is not set}"
: "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is not set}"

echo ">>> Checking prerequisites"

# Проверяем наличие jq
if ! command -v jq &> /dev/null; then
    echo "ERROR: jq is not installed"
    exit 1
fi

# Проверяем существование browsers.json файла
if [[ ! -f "$json_file" ]]; then
    echo "ERROR: browsers.json not found: $json_file"
    exit 1
fi

echo ">>> Pulling browser images"

# Извлекаем список образов из browsers.json и скачиваем каждый
while IFS= read -r image; do
    [[ -z "$image" ]] && continue

    echo "Pulling $image..."
    docker pull "$image"

done < <(jq -r '.. | objects | .image? // empty' "$json_file")

echo ">>> Stopping Docker Compose"

docker compose -f "$COMPOSE_FILE" down

echo ">>> Starting Docker Compose"

docker compose -f "$COMPOSE_FILE" up -d

echo ">>> Infrastructure started successfully"
