#!/usr/bin/env sh
echo "...starting..."
rustc *.rs -o /tmp/main && /tmp/main
