@echo off
rem Arrete tous les services (ordre inverse du demarrage)
cd /d "%~dp0"
for %%s in (frontend gateway inscription cours etudiant discovery) do call "%~dp0stop-service.bat" %%s
