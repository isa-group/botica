#!/bin/bash

env_file="bots.txt"
image_name="bot-ica:latest"

while IFS= read -r line; do
    if [ -z "$line" ] || [[ "$line" == \#* ]]; then
        continue
    fi

    if [[ "$line" =~ /app/src/main/resources/ConfigurationFiles/(.+)\.properties ]]; then
        identifier="${BASH_REMATCH[1]}"
    else
        echo "Error: The identifier could not be extracted"
        continue
    fi

    if [ $(docker ps -aq -f name=$identifier) ]; then
        docker rm -f $identifier
    fi

    docker run -d --name $identifier \
        --network botica_rabbitmq-network \
        -e BOT_PROPERTY_FILE_PATH=$line \
        -v $(pwd)/allure:/app/allure \
        -v $(pwd)/target:/app/target \
        -v $(pwd)/src/main/resources:/app/src/main/resources \
        $image_name
done < "$env_file"
