@echo off
chcp 65001 > nul
echo =======================================================================
echo   SISTEMA DE GESTÃO DE ILUMINAÇÃO INTELIGENTE PARA MINAS - SENAI BETIM
echo   INICIALIZAÇÃO LOCAL (BACKEND SPRING BOOT + FRONTEND VUE 3)
echo =======================================================================
echo.

echo [1/2] Iniciando Backend Spring Boot (Java 17 / H2 DB Dev)...
start "Backend Spring Boot" cmd /k "cd backend && "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" spring-boot:run"

echo Aguardando 5 segundos para o backend inicializar...
timeout /t 5 /nobreak > nul

echo [2/2] Iniciando Frontend Vue 3 (Vite)...
start "Frontend Vue 3" cmd /k "cd frontend && npm run dev"

echo.
echo =======================================================================
echo  APLICAÇÃO INICIADA COM SUCESSO!
echo  - Frontend Web Gestores & App: http://localhost:5173
echo  - Backend API:                 http://localhost:8080
echo  - Documentação Swagger:        http://localhost:8080/swagger-ui.html
echo  - Console Banco H2:            http://localhost:8080/h2-console
echo.
echo  Credenciais de Acesso:
echo    * Gestor CCO (Admin):        admin@mineracao.com.br    / admin123
echo    * Operador de Campo:         operador@mineracao.com.br / operador123
echo =======================================================================
pause
