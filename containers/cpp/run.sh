#!/usr/bin/env sh
echo "...starting..."
clang++ -std=c++20 -Wall -Wextra -Werror -pedantic -o /tmp/main *.cpp && /tmp/main
