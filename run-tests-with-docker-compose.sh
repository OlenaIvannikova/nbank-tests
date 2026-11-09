#!/bin/bash

# Запуск скрипта: ./run-tests-with-docker-compose.sh

# Определяем корневую директорию проекта и переходим в нее
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Путь к Docker Compose
COMPOSE_FILE="$SCRIPT_DIR/infra/docker_compose/docker-compose.yml"
ENV_FILE="$SCRIPT_DIR/.env"

# Проверяем наличие .env
if [[ ! -f "$ENV_FILE" ]]; then
    echo "Ошибка: файл .env не найден"
    exit 1
fi

# 1. Настройка переменных (параметры будущего запуска):
  # Имя Docker Image с тестами, который будем запускать
IMAGE_NAME=olenaivannikova1/nbank-tests:tests_with_database

  # Передача аргумента при запуске Bash-скрипта
  # Формируем директории, в которых будут храниться результаты конкретного запуска
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP
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
echo ">>> Запуск инфраструктуры"
docker compose \
    --env-file "$SCRIPT_DIR/.env" \
    -f "$COMPOSE_FILE" \
    up -d --wait

if [ $? -ne 0 ]; then
    echo "Ошибка запуска инфраструктуры"
    exit 1
fi

echo ">>> Инфраструктура успешно запущена"

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
docker run --rm \
  --network nbank-network \
  -v "$API_TESTS_OUTPUT_DIR/logs":/app/logs \
  -v "$API_TESTS_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$API_TESTS_OUTPUT_DIR/report":/app/target/reports \
  -e TEST_PROFILE=api \
  -e APIBASEURL=http://backend:4111 \
  -e UIBASEURL=http://frontend:80 \
  -e UIREMOTE=http://selenoid:4444/wd/hub \
  -e DB_URL=jdbc:postgresql://postgres:5432/nbank \
  --env-file "$SCRIPT_DIR/.env" \
$IMAGE_NAME

API_TEST_EXIT_CODE=$?

echo ">>> Запуск UI тестов"
docker run --rm \
  --network nbank-network \
  -v "$UI_TESTS_OUTPUT_DIR/logs":/app/logs \
  -v "$UI_TESTS_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$UI_TESTS_OUTPUT_DIR/report":/app/target/reports \
  -e TEST_PROFILE=ui \
  -e APIBASEURL=http://backend:4111 \
  -e UIBASEURL=http://frontend:80 \
  -e UIREMOTE=http://selenoid:4444/wd/hub \
  -e DB_URL=jdbc:postgresql://postgres:5432/nbank \
  --env-file "$SCRIPT_DIR/.env" \
$IMAGE_NAME

UI_TEST_EXIT_CODE=$?

# 5. Вывод результатов
echo ""
echo "========== РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ =========="

echo "API tests exit code: $API_TEST_EXIT_CODE"
echo "UI tests exit code: $UI_TEST_EXIT_CODE"

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