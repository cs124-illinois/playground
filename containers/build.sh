#!/usr/bin/env bash
for d in ./*/ ; do (cd "$d" && docker buildx build --platform=linux/amd64,linux/arm64 . && docker build -t cs124/playground-runner-${PWD##*/}:latest .); done
# vim: tw=0
