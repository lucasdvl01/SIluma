@echo off
chcp 65001 > nul
title Sistema CCO Iluminação Minas - SENAI Betim
echo =======================================================================
echo   SISTEMA DE GESTÃO DE ILUMINAÇÃO INTELIGENTE PARA MINAS - SENAI BETIM
echo   INICIALIZAÇÃO LOCAL (BACKEND SPRING BOOT + FRONTEND VUE 3)
echo =======================================================================
echo.

set "ROOT_DIR=%~dp0"

echo [1/2] Iniciando Backend Spring Boot (Java 17 / H2 DB Dev)...
start "Backend Spring Boot - CCO Minas" /D "%ROOT_DIR%backend" cmd /k "mvnw.cmd spring-boot:run"

echo Aguardando inicialização completa do backend Spring Boot...
set /a retries=0
:wait_backend
timeout /t 2 /nobreak > nul
curl -s -f http://localhost:8080/v3/api-docs > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    set /a retries+=1
    if %retries% LSS 18 (
        goto wait_backend
    )
)

echo [2/2] Iniciando Frontend Vue 3 (Vite)...
start "Frontend Vue 3 - CCO Minas" /D "%ROOT_DIR%frontend" cmd /k "npm run dev"

echo.
echo =======================================================================
echo  APLICAÇÃO INICIADA COM SUCESSO!
echo  - Frontend Web CCO:         http://localhost:5173
echo  - Backend REST API:         http://localhost:8080
echo  - Documentação Swagger:     http://localhost:8080/swagger-ui.html
echo  - Console Banco H2:         http://localhost:8080/h2-console
echo.
echo  Credenciais de Demonstração:
echo    * Gestor CCO (Admin):     admin (ou admin@mineracao.com.br) / admin123
echo    * Operador de Campo:      operador (ou operador@mineracao.com.br) / operador123
echo =======================================================================
echo.
echo Pressione qualquer tecla para abrir o CCO no seu navegador...
pause > nul
start http://localhost:5173
