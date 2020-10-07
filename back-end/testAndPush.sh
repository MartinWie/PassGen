#!/usr/bin/env bash

#this script should run tests and if they work, push to master.

if mvn test; then
  echo "Test Succeeded. "
  echo "Pushing to master."
  git push
else
    echo "Test failed."
    exit 1
fi