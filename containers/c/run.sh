#!/usr/bin/env sh
echo "...starting..."
clang -std=c11 -Wall -Wextra -Werror -pedantic -o /tmp/main *.c && /tmp/main
