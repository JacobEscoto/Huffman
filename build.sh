#!/bin/env bash

set -euo pipefail

if ! command -v javac >/dev/null 2>&1; then
  echo "Error: javac not found. Install the JDK first." >&2
  exit 1
fi

javac *.java
