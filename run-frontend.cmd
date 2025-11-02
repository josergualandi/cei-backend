@echo off
setlocal
cd /d "C:\Users\José Gualandi\cei-frontend-split"
echo Iniciando Frontend (Angular) em %CD% ...
if not exist node_modules (
  echo Instalando dependencias (npm ci)...
  call npm ci
)
call npm start
endlocal
