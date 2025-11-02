@echo off
setlocal
cd /d "C:\Users\José Gualandi\cei-backend-split"
echo Iniciando Backend (Spring Boot) em %CD% ...
call mvnw.cmd -q -DskipTests spring-boot:run
endlocal
