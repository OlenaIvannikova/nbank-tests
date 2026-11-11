#!/bin/bash

# Скрипт запускает API и UI тесты, собирает результаты тестирования и останавливает инфраструктуру
# Запуск скрипта: ./run-tests-with-docker-compose.sh

# Режимы безопасности Bash
set -euo pipefail

# Определяем корневую директорию проекта
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Пути к конфигурационным файлам
COMPOSE_FILE="$SCRIPT_DIR/infra/docker_compose/docker-compose.yml"
ENV_FILE="$SCRIPT_DIR/.env"

if [[ ! -f "$ENV_FILE" ]]; then
    echo "ERROR: .env file not found: $ENV_FILE"
    exit 1
fi

# 1. Настройка переменных (параметры будущего запуска):
  # Имя Docker Image с тестами, который будем запускать
IMAGE_NAME=olenaivannikova1/nbank-tests:tests_with_database

  # Передача аргумента при запуске Bash-скрипта
  # Формируем директории, в которых будут храниться результаты конкретного запуска
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
TEST_OUTPUT_DIR="./test-output/$TIMESTAMP"
API_TESTS_OUTPUT_DIR="$TEST_OUTPUT_DIR/api"
UI_TESTS_OUTPUT_DIR="$TEST_OUTPUT_DIR/ui"

  # Коды завершения тестов
API_TEST_EXIT_CODE=0
UI_TEST_EXIT_CODE=0

# При завершении скрипта останавливаем инфраструктуру
cleanup() {
    local EXIT_CODE=$?

    echo ">>> Остановка Docker Compose"

    docker compose \
        --env-file "$ENV_FILE" \
        -f "$COMPOSE_FILE" \
        down || echo "Предупреждение: не удалось полностью остановить инфраструктуру"

    exit "$EXIT_CODE"
}

# Когда Bash-скрипт завершится по любой причине, выполняем функцию clean
trap cleanup EXIT

# 2. Запуск инфраструктуры
INFRA_SCRIPT="$SCRIPT_DIR/infra/docker_compose/start-infrastructure.sh"

echo ">>> Запуск инфраструктуры"
if bash "$INFRA_SCRIPT"; then
    echo "Инфраструктура успешно запущена"
else
    INFRA_EXIT_CODE=$?
    echo "Ошибка запуска инфраструктуры. Exit code: $INFRA_EXIT_CODE"
    exit "$INFRA_EXIT_CODE"
fi

# 3.1 Создание директорий для результатов API тестов на Host
mkdir -p "$API_TESTS_OUTPUT_DIR/logs"
mkdir -p "$API_TESTS_OUTPUT_DIR/results"
mkdir -p "$API_TESTS_OUTPUT_DIR/report"

# 3.2 Создание директорий для результатов UI тестов на Host
mkdir -p "$UI_TESTS_OUTPUT_DIR/logs"
mkdir -p "$UI_TESTS_OUTPUT_DIR/results"
mkdir -p "$UI_TESTS_OUTPUT_DIR/report"

# 4. Запуск Docker Container с тестами
  # --rm — автоматически удаляет контейнер после его завершения
  # volumes — передача результатов из контейнера на Host
echo ">>> Запуск API тестов"
if docker run --rm \
    --network nbank-network \
    -v "$API_TESTS_OUTPUT_DIR/logs":/app/logs \
    -v "$API_TESTS_OUTPUT_DIR/results":/app/target/surefire-reports \
    -v "$API_TESTS_OUTPUT_DIR/report":/app/target/reports \
    -e TEST_PROFILE=api \
    -e APIBASEURL=http://backend:4111 \
    -e UIBASEURL=http://frontend:80 \
    -e UIREMOTE=http://selenoid:4444/wd/hub \
    -e DB_URL=jdbc:postgresql://postgres:5432/nbank \
    --env-file "$ENV_FILE" \
    "$IMAGE_NAME"; then

    API_TEST_EXIT_CODE=0

else
    API_TEST_EXIT_CODE=$?
fi

echo "API tests exit code: $API_TEST_EXIT_CODE"

echo ">>> Запуск UI тестов"
if docker run --rm \
    --network nbank-network \
    -v "$UI_TESTS_OUTPUT_DIR/logs":/app/logs \
    -v "$UI_TESTS_OUTPUT_DIR/results":/app/target/surefire-reports \
    -v "$UI_TESTS_OUTPUT_DIR/report":/app/target/reports \
    -e TEST_PROFILE=ui \
    -e APIBASEURL=http://backend:4111 \
    -e UIBASEURL=http://frontend:80 \
    -e UIREMOTE=http://selenoid:4444/wd/hub \
    -e DB_URL=jdbc:postgresql://postgres:5432/nbank \
     --env-file "$ENV_FILE" \
    "$IMAGE_NAME"; then

    UI_TEST_EXIT_CODE=0

else
    UI_TEST_EXIT_CODE=$?
fi

echo "UI tests exit code: $UI_TEST_EXIT_CODE"

# 5. Вывод результатов
echo ""
echo "========== РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ =========="

echo ""
echo ">>> Результаты API тестов"
echo "Лог файл: $API_TESTS_OUTPUT_DIR/logs/run.log"
echo "Результаты API тестов: $API_TESTS_OUTPUT_DIR/results"
echo "Репорт: $API_TESTS_OUTPUT_DIR/report"

echo ""
echo ">>> Результаты UI тестов"
echo "Лог файл: $UI_TESTS_OUTPUT_DIR/logs/run.log"
echo "Результаты UI тестов: $UI_TESTS_OUTPUT_DIR/results"
echo "Репорт: $UI_TESTS_OUTPUT_DIR/report"

# 6. Общий код завершения
if [[ "$API_TEST_EXIT_CODE" -ne 0 || "$UI_TEST_EXIT_CODE" -ne 0 ]]; then
    echo ""
    echo ">>> Тестирование завершилось с ошибками"
    exit 1
fi

echo ""
echo ">>> Все тесты пройдены успешно"
exit 0