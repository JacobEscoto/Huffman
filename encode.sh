#!/bin/env bash

set -euo pipefail

usage() {
  echo "Usage: ./encode.sh \"TEXT\""
  echo "       ./encode.sh < input.txt"
}

if [[ $# -lt 1 || $# -gt 1 ]]; then
  usage
  exit 1
fi

if [[ -f "$1" ]]; then
  TEXT="$(cat "$1")"
else
  TEXT="$1"
fi

java Main "$TEXT"
