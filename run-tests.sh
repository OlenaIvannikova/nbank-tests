#!/bin/bash

#### Этот Bash-скрипт автоматизирует весь процесс запуска тестов в Docker-е
#### При запуске командой bash run-tests.sh скрипт:
# 1) Выбирает Maven profile
# 2) Создаёт timestamp
# 3) Запускает docker build и собирает Docker Image
# 4) Создаёт папки для результатов на Host
# 5) Запускает контейнер c тестами с нужным профилем
# 6) Сохраняет логи и отчёты на компьютере (Host)


# Определяем корневую директорию проекта и переходим в нее
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Путь к Docker Compose
COMPOSE_FILE="$SCRIPT_DIR/infra/docker_compose/docker-compose.yml"

# 1. Настройка переменных (параметры будущего запуска):

  # Имя Docker Image, который мы будем собирать
IMAGE_NAME=nbank-tests

  # Передача аргумента при запуске Bash-скрипта
    #- $1 — первый аргумент, переданный скрипту
    #- :-api — если первый аргумент не передан или пустой, использовать значение api.
TEST_PROFILE=${1:-api}

  # Формируем директорию, в которой будут храниться результаты конкретного запуска.
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

# При завершении скрипта останавливаем инфраструктуру
cleanup() {
    echo ">>> Остановка Docker Compose"
    docker compose -f "$COMPOSE_FILE" down
}

# Когда Bash-скрипт завершится по любой причине, выполни функцию clean
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

# 3. Сборка Docker Image - cоздаем Docker Image из инструкций Dockerfile
  # t означает tag — присвоить образу имя (и при необходимости тег)
  # Точка в конце означает текущую директорию как Docker build context (контекст сборки)
  # Docker получает доступ к файлам этой директории, чтобы использовать их в инструкциях COPY и ADD
echo ">>> Сборка Docker Image"
docker build -t $IMAGE_NAME .

if [ $? -ne 0 ]; then
    echo "Ошибка сборки Docker Image"
    exit 1
fi

# 4. Создание директорий для результатов на Host
mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"
mkdir -p "$TEST_OUTPUT_DIR/allure-results"

# 5. Запуск Docker Container с тестами
  # --rm — автоматически удаляет контейнер после его завершения
  # volumes — передача результатов из контейнера на Host
echo ">>> Запуск тестов"
docker run --rm \
  --network nbank-network \
  -v "$TEST_OUTPUT_DIR/logs":/app/logs \
  -v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$TEST_OUTPUT_DIR/report":/app/target/reports \
  -v "$TEST_OUTPUT_DIR/allure-results":/app/target/allure-results \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e APIBASEURL=http://backend:4111 \
  -e APIVERSION=/api/v1 \
  -e UIBASEURL=http://frontend:80 \
  -e UIREMOTE=http://selenoid:4444/wd/hub \
  -e DB_URL=jdbc:postgresql://postgres:5432/nbank \
  --env-file "$SCRIPT_DIR/.env" \
$IMAGE_NAME

TEST_EXIT_CODE=$?

# 6. Вывод итогов (команды выводят в терминал, где искать результаты)
echo ">>> Тесты завершены"
echo "Лог файл: $TEST_OUTPUT_DIR/logs/run.log"
echo "Результаты тестов: $TEST_OUTPUT_DIR/results"
echo "Репорт Surefire: $TEST_OUTPUT_DIR/report"
echo "Allure results: $TEST_OUTPUT_DIR/allure-results"

# Генерация Allure HTML report
echo ">>> Генерация Allure report"

mkdir -p "$TEST_OUTPUT_DIR/allure-report"

if find "$TEST_OUTPUT_DIR/allure-results" \
    -name '*-result.json' -print -quit | grep -q .; then

# generate — команда на генерацию
# target/allure-results (в нашем случае рез-ты в "$TEST_OUTPUT_DIR/allure-results") — путь к результатам
# --clean — очистить предыдущий отчет
# -o ./allure-report (в нашем случае кладем в "$TEST_OUTPUT_DIR/allure-report") — выходная директория

    allure generate \
        "$TEST_OUTPUT_DIR/allure-results" \
        --clean \
        -o "$TEST_OUTPUT_DIR/allure-report"

    REPORT_EXIT_CODE=$?

    if [ "$REPORT_EXIT_CODE" -eq 0 ]; then
        echo "Allure report: $TEST_OUTPUT_DIR/allure-report/index.html"
    else
        echo "ERROR: Не удалось сгенерировать Allure report"
    fi
else
    echo "WARNING: Allure results не найдены. Отчёт не создан."
    REPORT_EXIT_CODE=1
fi

# Возвращаем код завершения тестов
if [ "$TEST_EXIT_CODE" -ne 0 ]; then
    exit "$TEST_EXIT_CODE"
fi

# Если тесты прошли, но отчёт не создался — возвращаем ошибку отчёта
exit "$REPORT_EXIT_CODE"