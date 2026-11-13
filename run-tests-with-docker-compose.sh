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

# 1. Настройка переменных (параметры будущего запуска)

# Имя Docker Image с тестами, который будем запускать
IMAGE_NAME=nbank-tests

# Формируем директории, в которых будут храниться результаты конкретного запуска
# Timestamp текущего запуска
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Общая директория результатов текущего запуска
TEST_OUTPUT_DIR="$SCRIPT_DIR/test-output/$TIMESTAMP"

# Директории API тестов
API_TESTS_OUTPUT_DIR="$TEST_OUTPUT_DIR/api"

# Директории UI тестов
UI_TESTS_OUTPUT_DIR="$TEST_OUTPUT_DIR/ui"

# Общие директории Allure
ALLURE_RESULTS_DIR="$TEST_OUTPUT_DIR/allure-results"
ALLURE_REPORT_DIR="$TEST_OUTPUT_DIR/allure-report"

# Директория для Swagger Coverage
SWAGGER_COVERAGE_DIR="$API_TESTS_OUTPUT_DIR/swagger-coverage-output"
SWAGGER_COVERAGE_REPORT_DIR="$API_TESTS_OUTPUT_DIR/swagger-coverage-report"

# Коды завершения тестов и генерации отчётов
API_TEST_EXIT_CODE=0
UI_TEST_EXIT_CODE=0
REPORT_EXIT_CODE=0
SWAGGER_COVERAGE_EXIT_CODE=0

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

# 2. Запуск инфраструктуры и сборка Docker Image из текущего состояния проекта

# Запуск инфраструктуры
INFRA_SCRIPT="$SCRIPT_DIR/infra/docker_compose/start-infrastructure.sh"

echo ">>> Запуск инфраструктуры"
if bash "$INFRA_SCRIPT"; then
    echo "Инфраструктура успешно запущена"
else
    INFRA_EXIT_CODE=$?
    echo "Ошибка запуска инфраструктуры. Exit code: $INFRA_EXIT_CODE"
    exit "$INFRA_EXIT_CODE"
fi

# Сборка Docker Image
echo ">>> Сборка Docker Image"

docker build -t "$IMAGE_NAME" "$SCRIPT_DIR"

# 3. Создание директорий для результатов
echo ">>> Создание директорий результатов"

# API
mkdir -p "$API_TESTS_OUTPUT_DIR/logs"
mkdir -p "$API_TESTS_OUTPUT_DIR/results"
mkdir -p "$API_TESTS_OUTPUT_DIR/report"
mkdir -p "$API_TESTS_OUTPUT_DIR/allure-results"
mkdir -p "$SWAGGER_COVERAGE_DIR"
mkdir -p "$SWAGGER_COVERAGE_REPORT_DIR"

# UI
mkdir -p "$UI_TESTS_OUTPUT_DIR/logs"
mkdir -p "$UI_TESTS_OUTPUT_DIR/results"
mkdir -p "$UI_TESTS_OUTPUT_DIR/report"
mkdir -p "$UI_TESTS_OUTPUT_DIR/allure-results"

# Общие Allure директории
mkdir -p "$ALLURE_RESULTS_DIR"
mkdir -p "$ALLURE_REPORT_DIR"

# 4. Запуск Docker Container с тестами
  # --rm — автоматически удаляет контейнер после его завершения
  # volumes — передача результатов из контейнера на Host
echo ">>> Запуск API тестов"
if docker run --rm \
    --network nbank-network \
    -v "$API_TESTS_OUTPUT_DIR/logs":/app/logs \
    -v "$API_TESTS_OUTPUT_DIR/results":/app/target/surefire-reports \
    -v "$API_TESTS_OUTPUT_DIR/report":/app/target/reports \
    -v "$API_TESTS_OUTPUT_DIR/allure-results":/app/target/allure-results \
    -v "$SWAGGER_COVERAGE_DIR:/app/target/swagger-coverage-output" \
    -e TEST_PROFILE=api \
    -e APIBASEURL=http://backend:4111 \
    -e APIVERSION=/api/v1 \
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

echo ">>> Генерация Swagger Coverage отчёта"

if (
    cd "$SWAGGER_COVERAGE_REPORT_DIR" &&
    "$SCRIPT_DIR/.swagger-coverage-commandline/bin/swagger-coverage-commandline" \
        -s "http://localhost:4111/v3/api-docs" \
        -i "$SWAGGER_COVERAGE_DIR"
); then
    echo ">>> Swagger Coverage отчёт успешно создан"
else
    SWAGGER_COVERAGE_EXIT_CODE=$?
    echo "!!! Ошибка генерации Swagger Coverage отчёта"
fi

echo ">>> Запуск UI тестов"
if docker run --rm \
    --network nbank-network \
    -v "$UI_TESTS_OUTPUT_DIR/logs":/app/logs \
    -v "$UI_TESTS_OUTPUT_DIR/results":/app/target/surefire-reports \
    -v "$UI_TESTS_OUTPUT_DIR/report":/app/target/reports \
    -v "$UI_TESTS_OUTPUT_DIR/allure-results":/app/target/allure-results \
    -e TEST_PROFILE=ui \
    -e APIBASEURL=http://backend:4111 \
    -e APIVERSION=/api/v1 \
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

# 5. Объединение Allure Results API + UI

echo ""
echo ">>> Объединение Allure Results"

# Копируем результаты API в общую директорию
if [[ -d "$API_TESTS_OUTPUT_DIR/allure-results" ]]; then
    cp -a "$API_TESTS_OUTPUT_DIR/allure-results/." "$ALLURE_RESULTS_DIR/"
fi

# Копируем результаты UI в общую директорию
if [[ -d "$UI_TESTS_OUTPUT_DIR/allure-results" ]]; then
    cp -a "$UI_TESTS_OUTPUT_DIR/allure-results/." "$ALLURE_RESULTS_DIR/"
fi

# Проверяем наличие результатов
RESULT_FILES=$(find "$ALLURE_RESULTS_DIR" \
    -type f -name '*-result.json' -print -quit)

# 6. Генерация общего Allure HTML Report

echo ""
echo ">>> Генерация общего Allure Report"

if [[ -z "$RESULT_FILES" ]]; then

    echo "WARNING: Allure results не найдены."
    REPORT_EXIT_CODE=1

elif ! command -v allure &> /dev/null; then

    echo "ERROR: Allure Commandline не установлен."
    REPORT_EXIT_CODE=1

else

    if allure generate \
        "$ALLURE_RESULTS_DIR" \
        --clean \
        -o "$ALLURE_REPORT_DIR"; then

        echo "Allure report успешно создан:"
        echo "$ALLURE_REPORT_DIR/index.html"

    else
        REPORT_EXIT_CODE=$?
        echo "ERROR: Не удалось сгенерировать Allure report."
    fi

fi

# 7. Вывод результатов
echo ""
echo "========== РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ =========="

echo ""
echo ">>> API результаты"
echo "Логи: $API_TESTS_OUTPUT_DIR/logs"
echo "Surefire: $API_TESTS_OUTPUT_DIR/results"
echo "Surefire report: $API_TESTS_OUTPUT_DIR/report"
echo "Allure results: $API_TESTS_OUTPUT_DIR/allure-results"
echo "Swagger Coverage results: $SWAGGER_COVERAGE_DIR"
echo "Swagger Coverage report: $SWAGGER_COVERAGE_REPORT_DIR"

echo ""
echo ">>> UI результаты"
echo "Логи: $UI_TESTS_OUTPUT_DIR/logs"
echo "Surefire: $UI_TESTS_OUTPUT_DIR/results"
echo "Surefire report: $UI_TESTS_OUTPUT_DIR/report"
echo "Allure results: $UI_TESTS_OUTPUT_DIR/allure-results"

echo ""
echo ">>> Общий Allure Report"
echo "Allure results: $ALLURE_RESULTS_DIR"
echo "Allure HTML: $ALLURE_REPORT_DIR/index.html"

# 8. Общий код завершения

if [[ "$API_TEST_EXIT_CODE" -ne 0 || "$UI_TEST_EXIT_CODE" -ne 0 ]]; then

    echo ""
    echo ">>> Тестирование завершилось с ошибками"
    exit 1

fi

if [[ "$REPORT_EXIT_CODE" -ne 0 ]]; then

    echo ""
    echo ">>> Тесты пройдены, но возникла ошибка генерации Allure Report"
    exit "$REPORT_EXIT_CODE"

fi

if [[ "$SWAGGER_COVERAGE_EXIT_CODE" -ne 0 ]]; then

    echo ""
    echo ">>> Тесты пройдены, Allure Report создан, но возникла ошибка генерации Swagger Coverage Report"
    exit "$SWAGGER_COVERAGE_EXIT_CODE"

fi

echo ""
echo ">>> Все тесты пройдены успешно"
echo ">>> Общий Allure Report создан успешно"
echo ">>> Swagger Coverage Report создан успешно"

exit 0