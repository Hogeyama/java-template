#!/usr/bin/env bash
set -euo pipefail
./gradlew bootRun >/dev/null &
spring=$!
trap 'kill $spring' EXIT
sleep 10 # TODO replace with health check
curl -fsSL http://localhost:8080/doc.yaml >openapi.yaml
