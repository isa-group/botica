#!/bin/bash

# Delete the container if it exists
if docker ps -a | grep -q dummy; then
    echo "Removing existing container..."
    docker rm dummy
fi

# Delete the volume if it exists
if docker volume ls | grep -q botica_botica-volume; then
    echo "Removing existing volume..."
    docker volume rm botica_botica-volume
fi

# Create container and volume
docker container create --name dummy -v botica_botica-volume:/app dummy
docker volume create --name botica_botica-volume

# Copy files to container
docker cp ./allure dummy:/app/allure

docker cp ./src/main/resources/Examples dummy:/app/src/main/resources/Examples
docker cp ./src/main/resources/ConfigurationFiles dummy:/app/src/main/resources/ConfigurationFiles
docker cp ./src/main/resources/allure-categories.json dummy:/app/src/main/resources/allure-categories.json
docker cp ./src/main/resources/config.properties dummy:/app/src/main/resources/config.properties
docker cp ./src/main/resources/fuzzing-dictionary.json dummy:/app/src/main/resources/fuzzing-dictionary.json

docker cp ./rabbitmq/server-config.json dummy:/app/rabbitmq/server-config.json
docker cp ./target dummy:/app/target

# Remove the container at the end
docker rm dummy

echo "Script completed successfully."