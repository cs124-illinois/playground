#!/usr/bin/env sh
echo "...starting..."
kotlinc *.kt -d /tmp/ && cd /tmp/ && java MainKt
