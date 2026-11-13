#!/bin/bash

# Скрипт проверяет prerequisites, загружает браузерные образы и запускает инфраструктуру

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
ENV_FILE="$PROJECT_DIR/.env"
# Путь к browsers.json
JSON_FILE="$PROJECT_DIR/selenoid/config/browsers.json"

echo ">>> Checking prerequisites"

# Проверяем наличие jq
if ! command -v jq &> /dev/null; then
    echo "ERROR: jq is not installed"
    exit 1
fi

# Проверяем существование browsers.json файла
if [[ ! -f "$JSON_FILE" ]]; then
    echo "ERROR: browsers.json not found: $JSON_FILE"
    exit 1
fi

# Проверяем существование docker-compose.yml файла
if [[ ! -f "$COMPOSE_FILE" ]]; then
    echo "ERROR: docker-compose.yml not found: $COMPOSE_FILE"
    exit 1
fi

# Проверяем существование .env файла
if [[ -f "$ENV_FILE" ]]; then
    echo ">>> Using .env file: $ENV_FILE"
    ENV_FILE_OPTION=(--env-file "$ENV_FILE")
else
    echo ">>> .env file not found. Using environment variables."
    ENV_FILE_OPTION=()
fi

echo ">>> Pulling browser images"

# Извлекаем список образов из browsers.json и скачиваем каждый образ
IMAGES=$(jq -r '.. | objects | .image? // empty' "$JSON_FILE")

while IFS= read -r image; do
    [[ -z "$image" ]] && continue

    echo "Pulling $image..."
    docker pull "$image"

done <<< "$IMAGES"

echo ">>> Starting Docker Compose"

docker compose \
    "${ENV_FILE_OPTION[@]}" \
    -f "$COMPOSE_FILE" \
    up -d --wait
