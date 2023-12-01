#!/bin/bash

# 1. Build the image located at ./docker/Dockerfile
echo "Building the image at ./docker/Dockerfile..."
docker build -t dummy ./docker

# 2. Build the image located at ./Dockerfile
echo "Building the image at ./Dockerfile..."
docker build -t bot-ica .

# 3. Start the data volume by executing ./docker/init_volume.sh
echo "Starting the data volume..."
chmod +x ./docker/init_volume.sh
./docker/init_volume.sh

# 4. Execute the docker-compose file located at ./docker-compose.yml
echo "Running docker-compose..."
docker-compose -f ./docker-compose.yml up -d

echo "Script completed successfully."
