@echo off
rem =====================================================================
rem  Demarrage complet de l'architecture Scolarite (Windows)
rem  Usage : start-all.bat              (build + demarrage, profil H2)
rem          start-all.bat --no-build   (demarrage sans recompiler)
rem          set SPRING_PROFILE=mysql ^& start-all.bat   (bases MySQL)
rem =====================================================================
setlocal EnableDelayedExpansion
chcp 65001 > nul
cd /d "%~dp0"
if not exist logs mkdir logs
if "%SPRING_PROFILE%"=="" set "SPRING_PROFILE=h2"
set "JAVA_OPTS=-Xms128m -Xmx384m -Dfile.encoding=UTF-8"
set "ARG1=%~1"

if /I not "%ARG1%"=="--no-build" (
  echo ==^> Build Maven : mvn clean package -DskipTests
  call mvn -q clean package -DskipTests
  if errorlevel 1 (
    echo     clean impossible - fichier verrouille ? - nouvelle tentative sans clean
    ping -n 4 127.0.0.1 > nul
    call mvn -q package -DskipTests
    if errorlevel 1 (
      echo ECHEC DU BUILD MAVEN
      exit /b 1
    )
  )
)

echo ==^> 1/4 discovery-server - Eureka, port 8761
call :demarrer discovery-server
rem pause de 15 s (ping fonctionne meme sans console interactive)
ping -n 16 127.0.0.1 > nul
call :attendre http://localhost:8761/actuator/health Eureka

echo ==^> 2/4 microservices metier - profil %SPRING_PROFILE%
call :demarrer etudiant-service
call :demarrer cours-service
call :demarrer inscription-service
call :attendre http://localhost:8081/actuator/health etudiant-service
call :attendre http://localhost:8082/actuator/health cours-service
call :attendre http://localhost:8083/actuator/health inscription-service

echo ==^> 3/4 api-gateway - port 8888
call :demarrer api-gateway
call :attendre http://localhost:8888/actuator/health api-gateway

echo ==^> 4/4 frontend - port 5173
where npm > nul 2>&1
if errorlevel 1 goto sansfront
if not exist frontend goto sansfront
pushd frontend
if not exist node_modules call npm install --no-audit --no-fund > ..\logs\frontend-install.log 2>&1
if /I not "%ARG1%"=="--no-build" call npm run build > ..\logs\frontend-build.log 2>&1
if exist dist (
  start "frontend" /min cmd /c "npm run preview > ..\logs\frontend.log 2>&1"
) else (
  start "frontend" /min cmd /c "npm run dev > ..\logs\frontend.log 2>&1"
)
popd
echo   -^> frontend demarre - logs\frontend.log
goto fin

:sansfront
echo   npm ou dossier frontend introuvable : frontend ignore

:fin
echo.
echo =====================================================================
echo   Eureka            : http://localhost:8761
echo   API Gateway       : http://localhost:8888/api/inscriptions/1
echo   Swagger agrege    : http://localhost:8888/swagger-ui.html
echo   Swagger services  : http://localhost:8081/swagger-ui.html - 8082, 8083
echo   Circuit breakers  : http://localhost:8083/actuator/circuitbreakers
echo   Frontend          : http://localhost:5173
echo   Panne simulee     : stop-service.bat etudiant
echo =====================================================================
endlocal
exit /b 0

rem ---------------------------------------------------------------------
:demarrer
start "%~1" /min cmd /c "java %JAVA_OPTS% -jar %~1\target\%~1-1.0.0.jar --spring.profiles.active=%SPRING_PROFILE% > logs\%~1.log 2>&1"
echo   -^> %~1 demarre - logs\%~1.log
exit /b 0

:attendre
set /a essais=0
:attendre_boucle
curl -s -o nul -w "%%{http_code}" %1 2>nul | findstr "200" > nul
if not errorlevel 1 (
  echo     %2 OK
  exit /b 0
)
set /a essais+=1
if !essais! geq 60 (
  echo     %2 : delai depasse, on continue
  exit /b 0
)
ping -n 3 127.0.0.1 > nul
goto attendre_boucle
