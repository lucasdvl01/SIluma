@echo off
chcp 65001 > nul
echo =======================================================================
echo   SISTEMA DE GESTÃO DE ILUMINAÇÃO INTELIGENTE PARA MINAS - SENAI BETIM
echo   INICIALIZAÇÃO VIA DOCKER COMPOSE (MySQL + Backend + Frontend)
echo =======================================================================
echo.
docker info > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] O serviço do Docker Desktop não está em execução no seu computador.
    echo.
    echo Sugestão:
    echo  1. Abra o aplicativo 'Docker Desktop' e aguarde inicializar, OU
    echo  2. Execute o arquivo 'start-local.bat' para rodar localmente sem precisar de Docker!
    echo.
    pause
    exit /b 1
)

docker compose up -d --build
echo.
echo =======================================================================
echo  CONTAINERS EM EXECUÇÃO!
echo  - Frontend Web & App:          http://localhost:80 (ou http://localhost:3000)
echo  - Backend Spring Boot API:     http://localhost:8080
echo  - Documentação Swagger:        http://localhost:8080/swagger-ui.html
echo  - Banco de Dados MySQL:        localhost:3306 (database: iluminacao_minas)
echo.
echo  Credenciais de Acesso:
echo    * Gestor CCO (Admin):        admin@mineracao.com.br    / admin123
echo    * Operador de Campo:         operador@mineracao.com.br / operador123
echo =======================================================================
pause
