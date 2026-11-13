#!/bin/bash

# Скрипт читает allure-report/widgets/coverage.json через jq и блокирует pipeline, если покрытие меньше 50%.

set -euo pipefail

COVERAGE_FILE="allure-report/widgets/coverage.json"

if [[ ! -f "$COVERAGE_FILE" ]]; then
    echo "ERROR: API coverage JSON not found"
    exit 1
fi

COVERAGE=$(jq -er '.coverage | numbers' "$COVERAGE_FILE")
THRESHOLD=$(jq -er '.threshold | numbers' "$COVERAGE_FILE")

echo "=================================="
echo "       API COVERAGE QUALITY GATE"
echo "=================================="

echo "Current coverage: $COVERAGE%"
echo "Required coverage: $THRESHOLD%"

if jq -e '.coverage >= .threshold' "$COVERAGE_FILE" > /dev/null; then
    echo "QUALITY GATE: PASSED"
    exit 0
else
    echo "QUALITY GATE: FAILED"
    echo "API coverage is below the required threshold!"
    exit 1
fi
