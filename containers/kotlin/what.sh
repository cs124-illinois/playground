#!/usr/bin/env sh
kotlin -version 2>&1 | head -n 1
tail -n -1 /run.sh
