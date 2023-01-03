#!/usr/bin/env sh
echo "...starting..."
scalac *.sc -d /tmp/ && cd /tmp/ && scala -Xmx64M Main
