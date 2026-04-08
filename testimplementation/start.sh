#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
DASHBOARD="$SCRIPT_DIR/dashboard/index.html"
ADMIN="$SCRIPT_DIR/admin/index.html"

PIDS=()

cleanup() {
    echo ""
    echo "Shutting down..."
    for pid in "${PIDS[@]}"; do
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null
            wait "$pid" 2>/dev/null || true
        fi
    done
    echo "All services stopped."
    exit 0
}

trap cleanup INT TERM

echo "Starting backend..."
cd "$BACKEND_DIR"
./gradlew bootRun --quiet &
PIDS+=($!)

echo "Waiting for backend to be ready..."
until curl -s http://localhost:8080/api/v1/dashboards > /dev/null 2>&1; do
    sleep 1
done
echo "Backend is ready at http://localhost:8080"

echo "Starting dashboard frontend..."
python3 -m http.server 3000 --directory "$SCRIPT_DIR/dashboard" &
PIDS+=($!)
echo "Dashboard available at http://localhost:3000"

echo "Starting admin frontend..."
python3 -m http.server 3001 --directory "$SCRIPT_DIR/admin" &
PIDS+=($!)
echo "Admin available at http://localhost:3001"

echo ""
echo "=== All services running ==="
echo "  Backend:   http://localhost:8080"
echo "  Dashboard: http://localhost:3000"
echo "  Admin:     http://localhost:3001"
echo "  H2 Console: http://localhost:8080/h2-console"
echo ""
echo "Press Ctrl+C to stop all services."

wait
