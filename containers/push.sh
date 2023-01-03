#!/usr/bin/env bash
for d in ./*/ ; do (cd "$d" && docker push cs124/playground-runner-${PWD##*/}:latest); done
# for d in ./*/ ; do (cd "$d" && docker buildx build --platform=linux/amd64,linux/arm64 . --builder multiplatform --push -t cs124/playground-runner-${PWD##*/}:latest); done
