#!/usr/bin/env sh
echo "...starting..."
ghc -v0 -o /tmp/main main.hs && /tmp/main
