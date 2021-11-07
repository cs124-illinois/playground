#!/usr/bin/env bash
for d in ./*/ ; do (cd "$d" && docker run cs124/playground-runner-${PWD##*/} /what.sh); done
