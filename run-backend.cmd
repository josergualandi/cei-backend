@echo off
setlocal ENABLEDELAYEDEXPANSION

REM Ir para a pasta do backend relativa a este script
cd /d "%~dp0backend" || (
	echo [ERRO] Pasta ^"backend^" nao encontrada ao lado deste script.
	exit /b 1
)

REM Carregar variaveis do arquivo .env no raiz do repo (opcional)
set "ENV_FILE=%~dp0.env"
if exist "%ENV_FILE%" (
	for /f "usebackq eol=# tokens=1,2 delims==" %%A in ("%ENV_FILE%") do (
		if /I "%%~A"=="SERVER_PORT" set "SERVER_PORT=%%~B"
		if /I "%%~A"=="SPRING_DATASOURCE_URL" set "SPRING_DATASOURCE_URL=%%~B"
		if /I "%%~A"=="SPRING_DATASOURCE_USERNAME" set "SPRING_DATASOURCE_USERNAME=%%~B"
		if /I "%%~A"=="SPRING_DATASOURCE_PASSWORD" set "SPRING_DATASOURCE_PASSWORD=%%~B"
		if /I "%%~A"=="JWT_SECRET" set "JWT_SECRET=%%~B"
		if /I "%%~A"=="JWT_EXPIRATION" set "JWT_EXPIRATION=%%~B"
		if /I "%%~A"=="APP_ADMIN_EMAIL" set "APP_ADMIN_EMAIL=%%~B"
		if /I "%%~A"=="APP_ADMIN_PASSWORD" set "APP_ADMIN_PASSWORD=%%~B"
	)
)

echo Iniciando Backend (Spring Boot) em %CD% ...
call mvnw.cmd -q -DskipTests spring-boot:run
endlocal
