#!/usr/bin/env bash
for d in ./*/ ; do (cd "$d" && docker push cs124/playground-runner-${PWD##*/}); done
