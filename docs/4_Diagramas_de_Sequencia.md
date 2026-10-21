# Diagramas de Sequência da Aplicação
**Projeto:** Sistema de Gestão e Monitoramento de Iluminação Inteligente para Minas  
**Instituição:** SENAI Betim (CETEM Maria Madalena Nogueira)  
**Item Exigido:** Documentação Técnica Exigida - Item 5.4

---

## 1. Diagrama de Sequência: Envio de Comando de Alteração de Status da Torre
Representação da comunicação entre Frontend Vue 3, Backend Spring Boot 3 e Banco de Dados MySQL/H2 durante o acionamento de um comando operacional (ligar, desligar ou modo cerração):

```mermaid
sequenceDiagram
    autonumber
    actor Operador as Operador / Gestor CCO
    participant Frontend as Frontend (Vue 3 / Axios)
    participant SecFilter as JwtAuthenticationFilter
    participant Controller as TowerController
    participant Service as TowerService
    participant ActivityRepo as TowerActivityLogRepository
    participant TowerRepo as LightingTowerRepository
    participant AuditService as AuditService
    participant Database as Banco de Dados (MySQL)

    Operador->>Frontend: Clica em "Ligar Torre" ou "Modo Cerração"
    Frontend->>SecFilter: POST /api/towers/{id}/action + Bearer Token JWT
    SecFilter->>SecFilter: Valida assinatura JWT e extrai permissões (RBAC)
    SecFilter->>Controller: Encaminha requisição autenticada
    Controller->>Service: executeAction(id, actionDTO, userEmail, clientIp)
    
    Service->>TowerRepo: findById(id)
    TowerRepo->>Database: SELECT * FROM tb_torres_iluminacao WHERE id = ?
    Database-->>TowerRepo: Retorna dados da Torre
    TowerRepo-->>Service: Entidade LightingTower

    alt Comando LIGAR
        Service->>Service: Atualiza ligada=true, status=OPERACIONAL, dataUltimaAtivacao=now()
        Service->>ActivityRepo: save(novo TowerActivityLog)
        ActivityRepo->>Database: INSERT INTO tb_historico_atividade (...)
    else Comando DESLIGAR
        Service->>ActivityRepo: findTopByTorreIdAndDataHoraDesligouIsNull(...)
        Database-->>ActivityRepo: Retorna sessão aberta
        Service->>Service: Calcula minutos ativos, consumo kWh e incrementa horímetro
        Service->>ActivityRepo: save(atualiza sessão fechada)
        ActivityRepo->>Database: UPDATE tb_historico_atividade SET data_desligou = now() ...
    else Comando MODO CERRACAO
        Service->>Service: Atualiza modoOperacao=ALTA_VISIBILIDADE_CERRACAO, status=ALERTA_BAIXA_VISIBILIDADE
    end

    Service->>TowerRepo: save(tower)
    TowerRepo->>Database: UPDATE tb_torres_iluminacao SET ... WHERE id = ?
    
    Service->>AuditService: registrarLog(userEmail, acao, "LightingTower", id, detalhes, clientIp)
    AuditService->>Database: INSERT INTO tb_logs_auditoria (...)

    Service-->>Controller: Retorna TowerResponseDTO atualizado
    Controller-->>Frontend: HTTP 200 OK (JSON com novo status e horímetro)
    Frontend-->>Operador: Atualiza cor do pino no mapa (Verde/Amarelo) e exibe toast de confirmação
```

---

## 2. Diagrama de Sequência: Automação IoT por Sensor de Luminosidade (Arduino Mega)
Demonstra o fluxo do circuito demonstrativo: o Arduino Mega lê o LDR e o botão, e uma ponte no computador encaminha a telemetria ao backend e retorna o comando dos LEDs:

```mermaid
sequenceDiagram
    autonumber
    participant Sensor as Sensor LDR (A7)
    participant Mega as Arduino Mega (Firmware)
    participant Bridge as Ponte USB/HTTP (PowerShell)
    participant API as IoTController (/api/iot/telemetry)
    participant IoTService as IoTService
    participant TowerRepo as LightingTowerRepository
    participant TelemetryRepo as IoTTelemetryRepository
    participant Frontend as Frontend Web (Dashboard / Mapa)
    participant LED as LEDs indicadores (pinos 47, 49, 51)

    Sensor->>Mega: Leitura analógica de luz (< 300 = baixa visibilidade)
    Mega->>Bridge: Telemetria JSON pela USB
    Bridge->>API: HTTP POST { codigoTorre: "TR-001", luminosidadeLdr: 150, ... }
    API->>IoTService: processTelemetry(telemetryRequest, clientIp)
    
    IoTService->>TowerRepo: findByCodigoIdentificacao("TR-001")
    TowerRepo-->>IoTService: Entidade LightingTower
    
    Note over IoTService: Limiar LDR < 300 detectado!<br/>Ativa "Alerta de Baixa Visibilidade"
    IoTService->>IoTService: tower.setStatus(ALERTA_BAIXA_VISIBILIDADE)<br/>tower.setModoOperacao(ALTA_VISIBILIDADE_CERRACAO)
    IoTService->>TowerRepo: save(tower)
    
    IoTService->>TelemetryRepo: save(IoTTelemetry com histórico bruto)
    
    IoTService-->>API: TelemetryResponseDTO (instrucaoLed: true, modoLuz: CERRACAO, pwm: 255)
    API-->>Bridge: HTTP 200 OK com a cor sugerida
    
    Bridge->>Mega: Caractere serial da cor (A, V, R, Z ou O)
    Mega->>LED: Aciona o LED indicador correspondente
    
    Note over Frontend: Polling periódico (10s) detecta alteração
    Frontend->>API: GET /api/towers
    API-->>Frontend: Retorna status atualizado
    Frontend-->>Frontend: Transforma marcador de TR-001 em Amarelo Pulsante no Mapa e no Dashboard
```

---

## 3. Diagrama de Sequência: Operação Offline do Operador de Campo e Sincronização
Representa a tolerância a falhas e sincronização automática ao entrar e sair de áreas remotas sem sinal (RF13, RNF-A11, RNF-A14):

```mermaid
sequenceDiagram
    autonumber
    actor Operador as Operador de Campo
    participant App as Aplicativo Mobile / PWA
    participant LocalStorage as Cache Local / IndexedDB
    participant Backend as Backend Spring Boot

    Note over App: Operador entra em galeria sem sinal (Offline)
    App->>App: Evento 'offline' disparado -> isOnline = false
    Operador->>App: Clica em "Ligar Torre TR-004"
    App->>App: Emite vibração tátil (Web Vibration API)
    App->>LocalStorage: Grava comando na fila 'offline_commands_queue'
    App-->>Operador: Alerta: "Comando salvo na fila offline. Sincronização automática pendente."

    Note over App: Operador retorna à área com cobertura Wi-Fi/4G
    App->>App: Evento 'online' disparado -> isOnline = true
    App->>LocalStorage: Recupera comandos enfileirados
    LocalStorage-->>App: Lista de comandos pendentes

    loop Para cada comando pendente
        App->>Backend: POST /api/towers/{id}/action { acao: "LIGAR" }
        Backend-->>App: HTTP 200 OK
    end

    App->>LocalStorage: Limpa fila offline
    App-->>Operador: Notificação de sucesso: "Todos os comandos offline foram sincronizados!"
```
