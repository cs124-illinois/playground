#!/usr/bin/env sh
echo "...starting..."
javac *.java -d /tmp/ && cd /tmp/ && java -Xmx64M Main
