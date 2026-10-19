#!/bin/bash
echo "======================================================================"
echo "   INICIADOR DEL SISTEMA - PLATAFORMA DE TORNEOS ESPORTS (SCRUM)"
echo "======================================================================"
echo ""
echo "Requisitos mínimos: Java 17 o 21 instalado."
echo ""
chmod +x ./torneos-service/mvnw ./matchmaking-service/mvnw ./frontend-web/mvnw

echo "Iniciando Torneos Service [8081]..."
(cd torneos-service && ./mvnw spring-boot:run) &
PID_TORNEOS=$!

sleep 6

echo "Iniciando Matchmaking Service [8083]..."
(cd matchmaking-service && ./mvnw spring-boot:run) &
PID_MATCHMAKING=$!

sleep 6

echo "Iniciando Frontend Web [8082]..."
(cd frontend-web && ./mvnw spring-boot:run) &
PID_FRONTEND=$!

echo ""
echo "======================================================================"
echo "Servicios en ejecucion en segundo plano:"
echo "Web: http://localhost:8082/torneos"
echo "Torneos Swagger: http://localhost:8081/swagger-ui.html"
echo "Matchmaking Swagger: http://localhost:8083/swagger-ui.html"
echo "======================================================================"
echo "Presiona CTRL+C para detener todos los servicios."

trap "kill $PID_TORNEOS $PID_MATCHMAKING $PID_FRONTEND; exit" SIGINT SIGTERM
wait
