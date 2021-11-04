#!/usr/bin/env bash
for d in ./*/ ; do (cd "$d" && docker rmi cs124/playground-test-${PWD##*/}); done
