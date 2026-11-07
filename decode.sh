#!/bin/env bash

set -euo pipefail

usage() {
  echo "Usage: ./decode.sh \"TABLE\" \"BITS\""
}

if [[ $# -lt 2 || $# -gt 2 ]]; then
  usage
  exit 1
fi

if [[ -f "$1" ]]; then
  TABLE="$(cat "$1")"
else
  TABLE="$1"
fi

if [[ -f "$2" ]]; then
  BITS="$(cat "$2")"
else
  BITS="$2"
fi

java Main -decode "$TABLE" "$BITS"
