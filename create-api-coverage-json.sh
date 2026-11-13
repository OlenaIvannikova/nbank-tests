#!/bin/bash

# Скрипт
# 1. Читает swagger-coverage-results.json файл
# 2. Извлекает из него conditionCounter.covered и conditionCounter.all
#   "conditionCounter": {
#        "all": 3,
#        "covered": 3,
#        "deprecated": 0,
#        "deprecatedAndEmpty": 0
#      }
# 3. Вычисляет % покрытия
# 4. Создает файл allure-report/widgets/coverage.json вида
#  {
#    "name": "API Coverage",
#    "metric": "Condition Coverage",
#    "covered": 20,
#    "total": 29,
#    "coverage": 68.97,
#    "threshold": 50,
#    "status": "PASSED"
#  }

set -euo pipefail

SOURCE_FILE="swagger-coverage-results.json"
OUTPUT_FILE="allure-report/widgets/coverage.json"

# Quality Gate: если покрытие API < 50%, workflow падает
MIN_COVERAGE=50

if [[ ! -f "$SOURCE_FILE" ]]; then
    echo "ERROR: Swagger coverage results not found: $SOURCE_FILE"
    exit 1
fi

mkdir -p "$(dirname "$OUTPUT_FILE")"

jq -e '
    (.conditionCounter.all | numbers) and
    (.conditionCounter.covered | numbers) and
    (.conditionCounter.all > 0)
' "$SOURCE_FILE" > /dev/null

jq \
    --argjson threshold "$MIN_COVERAGE" \
    '
    .conditionCounter as $counter |
    ($counter.covered * 100 / $counter.all) as $coverage |

    {
        name: "API Coverage",
        metric: "Condition Coverage",
        covered: $counter.covered,
        total: $counter.all,
        coverage: (($coverage * 100 | round) / 100),
        threshold: $threshold,
        status: (
            if $coverage >= $threshold
            then "PASSED"
            else "FAILED"
            end
        )
    }
    ' "$SOURCE_FILE" > "$OUTPUT_FILE"

echo "API coverage JSON generated: $OUTPUT_FILE"
cat "$OUTPUT_FILE"
