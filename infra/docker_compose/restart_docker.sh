#!/bin/bash

# set -euo pipefail
  # Это 3 режима безопасности Bash:
  # -e	Остановить скрипт при ошибке команды
  # -u	Ошибка при использовании неопределённой переменной
  # -o pipefail	Ошибка в pipeline, если завершилась ошибкой любая команда
set -euo pipefail

echo ">>> Checking prerequisites"

# Проверяем наличие jq
if ! command -v jq &> /dev/null; then
    echo "ERROR: jq is not installed"
    exit 1
fi

# Путь к browsers.json
json_file="././selenoid/config/browsers.json"

# Проверяем существование файла
if [[ ! -f "$json_file" ]]; then
    echo "ERROR: browsers.json not found: $json_file"
    exit 1
fi

echo ">>> Pulling browser images"

# Извлекаем образы и скачиваем каждый
while IFS= read -r image; do
    [[ -z "$image" ]] && continue

    echo "Pulling $image..."
    docker pull "$image"

done < <(jq -r '.. | objects | .image? // empty' "$json_file")

echo ">>> Stopping Docker Compose"

docker compose down

echo ">>> Starting Docker Compose"

docker compose up -d

echo ">>> Infrastructure started successfully"
