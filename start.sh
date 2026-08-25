#!/usr/bin/env bash
set -e

echo "================================================================="
echo "  Future Transformation - PMTrack Platform One-Click Launcher"
echo "================================================================="
echo ""

echo "[1/2] Starting Spring Boot Backend API (Port 8080)..."
(cd backend && mvn spring-boot:run) &
BACKEND_PID=$!

echo "[2/2] Starting React Vite Frontend (Port 5173)..."
(cd frontend && npm run dev) &
FRONTEND_PID=$!

echo ""
echo "================================================================="
echo "  PMTrack is launching!"
echo "  Frontend: http://localhost:5173"
echo "  Backend API: http://localhost:8080"
echo "  Swagger UI: http://localhost:8080/swagger-ui.html"
echo "================================================================="

trap "kill $BACKEND_PID $FRONTEND_PID" EXIT
wait
