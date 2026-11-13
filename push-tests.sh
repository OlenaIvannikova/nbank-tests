#!/bin/bash

# Запуск скрипта: ./push-tests.sh

# Подключаем файл .env
set -a
source .env
set +a

# Переменные для имени образа и тега
IMAGE_NAME=nbank-tests
TAG=tests_with_database

# Авторизация в Docker Hub через --password-stdin
echo ">>> Login to Docker Hub"
echo "$DOCKERHUB_TOKEN" | docker login \
  -u "$DOCKERHUB_USERNAME" \
  --password-stdin

if [ $? -ne 0 ]; then
    echo "ERROR: Docker Hub login failed!"
    exit 1
fi

# Tегирование образа в формате <dockerhub-username>/<image-name>:<tag>
echo ">>> Building Docker Image"
docker build -t "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG" .

# Push образа в Docker Hub
echo ">>> Pushing Docker Image"
docker image push "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"

if [ $? -ne 0 ]; then
    echo "ERROR: Docker image push failed!"
    exit 1
fi

# Финальное сообщение с тем, как скачать образ командой docker pull
echo ">>> Download image using:"
echo ">>> docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"