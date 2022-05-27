#!/usr/bin/env sh
echo "...starting..."
swiftc main.swift -o /tmp/main && /tmp/main
