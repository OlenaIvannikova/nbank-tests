# FROM базовый докер образ
# Зачем каждый раз с нуля строить базовый образ (java, mvn, git) (голый докер образ)
# и каждый раз устанавлить java, maven и т.д,
# ЕСЛИ МОЖНО СОЗДАТЬ ОБРАЗ ПОВЕРХ ДРУГОГО ОБРАЗА, ГДЕ ВСЕ УЖЕ УСТАНОВЛЕНО
# МАРКЕТПЛЕЙС ВСЕХ ДОКЕР ОБРАЗОВ - docker hub
FROM maven:3.9.9-eclipse-temurin-21

# Дефолтные значения аргументов
ARG TEST_PROFILE=api
ARG APIBASEURL=http://localhost:4111
ARG UIBASEURL=http://localhost:3000

# Переменные окружения для контейнера
# Слева TEST_PROFILE — имя переменной, которая будет создана внутри контейнера
# Справа ${TEST_PROFILE} — значение, которое Docker пытается подставить при сборке образа
# Значения будут записаны в ENV образа и доступны контейнеру при запуске
ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}

# Задаёт рабочую директорию /app внутри Docker-контейнера,
# в которой будут выполняться последующие команды Dockerfile и команды запуска контейнера.
WORKDIR /app

# Копирует файл pom.xml с HOST-а внутрь текущей рабочей директории внутри Docker образа
COPY pom.xml .

# Docker Layer Caching
# Заранее загружает зависимости Maven-проекта,
# чтобы они были доступны локально (обычно /root/.m2/repository/) (в кэше)
# и не требовали скачивания при последующей сборке
RUN mvn dependency:go-offline

# Копирует всё содержимое Docker build context с HOST-а (весь проект) внутрь текущей рабочей директории Docker image.
COPY . .

# После этих команд внутри Docker image уже есть:
  #1. Maven и JDK 21 (из базового образа)
  #2. Maven-зависимости (загруженные командой dependency:go-offline)
  #3. Весь исходный код проекта (скопированный через COPY . .)

# Указывает, что все последующие команды Dockerfile будут выполняться
# от имени пользователя root внутри контейнера
USER root


# Инструкция CMD в Dockerfile. Она задаёт команду, которая выполнится при запуске контейнера (docker run)

# Т.е. при запуске Docker container CMD запускает Bash
# /bin/bash — запускает Bash внутри контейнера
# -c — говорит Bash: «выполни следующую строку как набор команд»:

  # 1.Создаётся директория /app/logs
  # mkdir -p /app/logs, где -p — создать и не выдавать ошибку, если директория уже существует
  # 2. Запускаются автотесты и формируется HTML-отчет
  # {} - фигурные скобки объединяют несколько команд в одну группу
    ####### Запуск автотестов: mvn test -q -P api
    # echo '>>> Running tests with profile: ${TEST_PROFILE}'
          # mvn test -q -P ${TEST_PROFILE} - -q — quiet mode, уменьшает количество сообщений Maven в консоли
    ####### Формирование отчета: mvn -DskipTests=true surfire-report:report
    ####### запускается Maven Surefire Report Plugin, отчет в /app/target/site/surefire-report.html
    # echo '>>> Running surefire-report:report'
         # mvn -DskipTests=true surefire-report:report
  # 3. Результаты в run.log файле (stdout + stderr)
  # > /app/logs/run.log 2>&1
    # > перенаправить и записать стандартный вывод (stdout) в файл /app/logs/run.log
    # 2>&1	- направить стандартный поток ошибок (stderr) туда же, куда направлен stdout
CMD /bin/bash -c " \
    mkdir -p /app/logs ; \
    { \
    echo '>>> Running tests with profile: ${TEST_PROFILE}' ; \
    mvn test -q -P ${TEST_PROFILE} ; \
    \
    echo '>>> Running surefire-report:report' ; \
    mvn -DskipTests=true surefire-report:report ; \
   } > /app/logs/run.log 2>&1"
