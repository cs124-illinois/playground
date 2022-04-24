#!/usr/bin/env bash
for d in ./*/ ; do (cd "$d" && docker pull cs124/playground-runner-${PWD##*/}:latest); done
