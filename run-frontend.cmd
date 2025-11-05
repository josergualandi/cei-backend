@echo off
setlocal

REM Ir para a pasta do frontend relativa a este script
cd /d "%~dp0frontend" || (
  echo [ERRO] Pasta ^"frontend^" nao encontrada ao lado deste script.
  exit /b 1
)

echo Iniciando Frontend (Angular) em %CD% ...
if not exist node_modules (
  echo Instalando dependencias: npm ci
  call npm ci
)
call npm start
endlocal
