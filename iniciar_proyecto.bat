@echo off
chcp 65001 > nul
title Iniciando Plataforma de Torneos Esports

echo ======================================================================
echo    INICIADOR DEL SISTEMA - PLATAFORMA DE TORNEOS ESPORTS (SCRUM)
echo ======================================================================
echo.
echo Requisitos minimos: Java 17 o 21 instalado en el sistema.
echo.
echo Iniciando Microservicio 1: Torneos Service (Puerto 8081)...
start "Torneos Service [8081]" cmd /k "cd /d torneos-service && mvnw.cmd spring-boot:run"

echo Esperando 6 segundos antes de iniciar el siguiente servicio...
timeout /t 6 /nobreak > nul

echo Iniciando Microservicio 2: Matchmaking Service (Puerto 8083)...
start "Matchmaking Service [8083]" cmd /k "cd /d matchmaking-service && mvnw.cmd spring-boot:run"

echo Esperando 6 segundos antes de iniciar la interfaz web...
timeout /t 6 /nobreak > nul

echo Iniciando Aplicacion Web: Frontend Web (Puerto 8082)...
start "Frontend Web [8082]" cmd /k "cd /d frontend-web && mvnw.cmd spring-boot:run"

echo.
echo ======================================================================
echo Todos los servicios han sido lanzados en terminales independientes.
echo.
echo Acceso a la aplicacion Web:
echo    http://localhost:8082/torneos
echo.
echo Documentacion de APIs (Swagger):
echo    http://localhost:8081/swagger-ui.html (Torneos)
echo    http://localhost:8083/swagger-ui.html (Matchmaking)
echo ======================================================================
echo Para detener los servicios, simplemente cierra cada ventana de consola.
pause
