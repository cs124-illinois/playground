#!/usr/bin/env bash
for d in ./*/ ; do (cd "$d" && docker build . -t cs124/playground-test-${PWD##*/}); done
