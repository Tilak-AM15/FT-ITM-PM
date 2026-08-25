@echo off
echo =================================================================
echo   Future Transformation - PMTrack Platform One-Click Launcher
echo =================================================================
echo.

echo [1/2] Starting Spring Boot Backend API (Port 8080)...
start "PMTrack Backend API" cmd /k "cd backend && mvn spring-boot:run"

echo [2/2] Starting React Vite Frontend (Port 5173)...
start "PMTrack Frontend UI" cmd /k "cd frontend && npm run dev"

echo.
echo =================================================================
echo   PMTrack is launching!
echo   Frontend: http://localhost:5173
echo   Backend API: http://localhost:8080
echo   Swagger UI: http://localhost:8080/swagger-ui.html
echo   H2 Console: http://localhost:8080/h2-console
echo =================================================================
pause
