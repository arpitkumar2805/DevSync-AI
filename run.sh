#!/usr/bin/env bash
#
# Single entry point to run the whole DevSync app locally:
#   - Postgres + Redis + the Spring Boot monolith (via devsync-ai/docker-compose.yml)
#   - The React/Vite frontend dev server
#
# Usage: ./run.sh
# Ctrl-C stops the frontend dev server and tears down the Docker containers.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/devsync-ai"
FRONTEND_DIR="$SCRIPT_DIR/devsync-frontend"
BACKEND_HEALTH_URL="http://localhost:8080/actuator/health"
BACKEND_READY_TIMEOUT=90

command -v docker >/dev/null 2>&1 || { echo "Error: docker is required but not found on PATH." >&2; exit 1; }
command -v npm >/dev/null 2>&1 || { echo "Error: npm is required but not found on PATH." >&2; exit 1; }

cleanup() {
  echo
  echo "Shutting down containers..."
  (cd "$BACKEND_DIR" && docker compose down) || true
}
trap cleanup EXIT INT TERM

echo "==> Starting Postgres, Redis, and the backend monolith (Docker)..."
(cd "$BACKEND_DIR" && docker compose up -d --build)

echo "==> Waiting for backend to become healthy at $BACKEND_HEALTH_URL ..."
elapsed=0
until curl -sf "$BACKEND_HEALTH_URL" | grep -q '"status":"UP"'; do
  if [ "$elapsed" -ge "$BACKEND_READY_TIMEOUT" ]; then
    echo "Error: backend did not become healthy within ${BACKEND_READY_TIMEOUT}s." >&2
    echo "Check logs with: (cd devsync-ai && docker compose logs devsync-monolith)" >&2
    exit 1
  fi
  sleep 3
  elapsed=$((elapsed + 3))
  echo "   ...still waiting (${elapsed}s elapsed)"
done
echo "==> Backend is healthy."

if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
  echo "==> Installing frontend dependencies (first run)..."
  (cd "$FRONTEND_DIR" && npm install)
fi

echo "==> Starting frontend dev server (http://localhost:5173)..."
(cd "$FRONTEND_DIR" && npm run dev)
