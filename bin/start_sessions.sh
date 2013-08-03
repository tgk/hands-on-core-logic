#!/usr/bin/env bash

N=$1
SESSION_BASEDIR=$2

for (( i = 0; i <= $N; i++))
do
  echo "Starting client number ${i}"
  NREPL_PORT=$((5000 + $i))
  echo "nREPL port: ${NREPL_PORT}"
  SESSION_PORT=$((8000 + $i))
  echo "session port: ${SESSION_PORT}"
  cd ${SESSION_BASEDIR}
  lein run "{:nrepl-uri \"nrepl://localhost:${NREPL_PORT}\" :port ${SESSION_PORT}}" &
  PID=$!
  echo "Session pid: ${PID}"

done
