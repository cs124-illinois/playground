#!/usr/bin/env bash
for d in ./*/ ; do (cd "$d" && docker run --platform=linux/amd64 cs124/playground-runner-${PWD##*/} /what.sh); done
