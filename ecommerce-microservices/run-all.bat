@echo off
title Sweet Shop E-Commerce Microservices Starter
echo ===================================================
echo   🧁 Starting Sweet Shop Microservices 🍭
echo ===================================================
echo.

:: 1. Start Discovery Server (Eureka)
echo [1/6] Launching Discovery Server (Eureka)...
start "Discovery Server (Port 8761)" cmd /k "mvn -pl discovery-server spring-boot:run"
echo Waiting for Eureka to warm up...
timeout /t 10

:: 2. Start Auth Service
echo [2/6] Launching Auth Service...
start "Auth Service (Port 8081)" cmd /k "set DB_PASSWORD=&& mvn -pl auth-service spring-boot:run"
timeout /t 5

:: 3. Start Product Service
echo [3/6] Launching Product Service...
start "Product Service (Port 8082)" cmd /k "set DB_PASSWORD=&& mvn -pl product-service spring-boot:run"
timeout /t 5

:: 4. Start Order Service
echo [4/6] Launching Order Service...
start "Order Service (Port 8083)" cmd /k "set DB_PASSWORD=&& mvn -pl order-service spring-boot:run"
timeout /t 5

:: 5. Start API Gateway
echo [5/6] Launching API Gateway...
start "API Gateway (Port 8080)" cmd /k "mvn -pl api-gateway spring-boot:run"
timeout /t 5

:: 6. Start React Frontend
echo [6/6] Launching React Frontend dev server...
start "React Frontend (Port 5173)" cmd /k "cd ecommerce-frontend && npm run dev"

echo.
echo ===================================================
echo   ✨ All services are spawning in new windows! ✨
echo   You can open http://localhost:5173 once ready.
echo ===================================================
pause
