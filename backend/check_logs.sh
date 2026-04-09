#!/bin/bash
# Wait for port 8080 to be responsive, then check if logs are appearing

PORT=8080
TIMEOUT=120
LOG_FILE="logs/application.log"

echo "Waiting for server on port $PORT..."

elapsed=0
while ! curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/actuator/health 2>/dev/null | grep -qE "^[2-5]"; do
  sleep 2
  elapsed=$((elapsed + 2))
  if [ $elapsed -ge $TIMEOUT ]; then
    echo "TIMEOUT: Server did not start within ${TIMEOUT}s"
    pkill -f "bootRun" 2>/dev/null
    exit 1
  fi
  echo "  ...waiting (${elapsed}s)"
done

echo "Server is UP on port $PORT"

# Check if log file has content
if [ -f "$LOG_FILE" ] && [ -s "$LOG_FILE" ]; then
  echo "LOGS OK: $LOG_FILE has content"
  tail -20 "$LOG_FILE"
else
  echo "NO LOGS: $LOG_FILE is missing or empty — killing server"
  pkill -f "bootRun" 2>/dev/null
  pkill -f "java.*builder" 2>/dev/null
  exit 2
fi
