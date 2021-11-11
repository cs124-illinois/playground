#!/usr/bin/env bash
for d in ./*/ ; do (cd "$d" && docker rmi -f cs124/playground-runner-${PWD##*/}); done
