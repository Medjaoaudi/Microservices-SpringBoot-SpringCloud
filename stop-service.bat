@echo off
rem =====================================================================
rem  Arrete UN service (simulation de panne pendant la demo)
rem  Usage : stop-service.bat etudiant^|cours^|inscription^|gateway^|discovery^|frontend
rem =====================================================================
setlocal
set "PORT="
if /I "%~1"=="etudiant" set "PORT=8081"
if /I "%~1"=="etudiant-service" set "PORT=8081"
if /I "%~1"=="cours" set "PORT=8082"
if /I "%~1"=="cours-service" set "PORT=8082"
if /I "%~1"=="inscription" set "PORT=8083"
if /I "%~1"=="inscription-service" set "PORT=8083"
if /I "%~1"=="gateway" set "PORT=8888"
if /I "%~1"=="api-gateway" set "PORT=8888"
if /I "%~1"=="discovery" set "PORT=8761"
if /I "%~1"=="eureka" set "PORT=8761"
if /I "%~1"=="frontend" set "PORT=5173"
if "%PORT%"=="" (
  echo Usage : stop-service.bat etudiant^|cours^|inscription^|gateway^|discovery^|frontend
  exit /b 1
)
echo ==^> Arret de %~1 - port %PORT%
set "TROUVE="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /R /C:":%PORT% .*LISTENING"') do (
  taskkill /PID %%p /F > nul 2>&1 && echo   processus %%p arrete
  set "TROUVE=1"
)
if not defined TROUVE echo   aucun processus n'ecoute sur le port %PORT%
endlocal
exit /b 0
