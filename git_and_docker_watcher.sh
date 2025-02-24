#!/bin/bash

REPO_DIR="~/PassGen"
CONTAINER_NAME="passgen"
IMAGE_NAME="passgen"

pushd $REPO_DIR

# Fetch the latest changes
git fetch

# Check if there are new commits
LOCAL=$(git rev-parse HEAD)
REMOTE=$(git rev-parse @{u})

if [ "$LOCAL" != "$REMOTE" ]; then
    echo "New updates found. Pulling changes and rebuilding the image..."
    curl -d "New $CONTAINER_NAME version found updating the service" ntfy.sh/<ID-here>

    # Pull the latest changes
    git pull

    # Trigger a full build
    echo "Building JAR"
    aenv -e Prod -s Passgen bash fullBuild.sh

    # Build the Docker image
    echo "Building docker image"
    docker build -t $IMAGE_NAME $REPO_DIR

    # Stop and remove the old container
    docker stop $CONTAINER_NAME
    docker rm $CONTAINER_NAME

    # Run a new container with the updated image, passing environment variables
    aenv -e Prod -s Passgen docker run -d -p 8080:8080 --name $CONTAINER_NAME --restart unless-stopped \
        -e SECRET_PASSGEN_DB-HOST=$SECRET_PASSGEN_DB-HOST \
        -e SECRET_PASSGEN_DB-USER=$SECRET_PASSGEN_DB-USER \
        -e SECRET_PASSGEN_DB-PASSWORD=$SECRET_PASSGEN_DB-PASSWORD \
        $IMAGE_NAME
else
    echo "No updates found. Container remains the same."
fi

popd

# Check container health status
STATUS=$(docker inspect --format='{{.State.Health.Status}}' $CONTAINER_NAME)

if [ "$STATUS" == "unhealthy" ]; then
    echo "Container is unhealthy. Restarting..."
    curl -d "$CONTAINER_NAME container is unhealthy. Restarting..." ntfy.sh/<ID-here>
    docker restart $CONTAINER_NAME
fi