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

## 2. Diagrama de Sequência: Automação IoT por Sensor de Luminosidade (LDR / ESP32)
Demonstra o fluxo automatizado da extensão IoT: o microcontrolador ESP32 detecta escuridão ou forte cerração na mina através do sensor LDR e envia telemetria ao backend, que altera o status da torre e responde com a instrução de acionamento do LED:

```mermaid
sequenceDiagram
    autonumber
    participant Sensor as Sensor LDR (GPIO 34)
    participant ESP32 as ESP32 / Arduino (Firmware / Simulador)
    participant API as IoTController (/api/iot/telemetry)
    participant IoTService as IoTService
    participant TowerRepo as LightingTowerRepository
    participant TelemetryRepo as IoTTelemetryRepository
    participant Frontend as Frontend Web (Dashboard / Mapa)
    participant LED as LED RGB da Luminária (GPIO 25, 26, 27)

    Sensor->>ESP32: Leitura analógica de luz (< 300 = escuridão/cerração)
    ESP32->>API: HTTP POST { codigoTorre: "TR-001", luminosidadeLdr: 150, ... }
    API->>IoTService: processTelemetry(telemetryRequest, clientIp)
    
    IoTService->>TowerRepo: findByCodigoIdentificacao("TR-001")
    TowerRepo-->>IoTService: Entidade LightingTower
    
    Note over IoTService: Limiar LDR < 300 detectado!<br/>Ativa "Alerta de Baixa Visibilidade"
    IoTService->>IoTService: tower.setStatus(ALERTA_BAIXA_VISIBILIDADE)<br/>tower.setModoOperacao(ALTA_VISIBILIDADE_CERRACAO)
    IoTService->>TowerRepo: save(tower)
    
    IoTService->>TelemetryRepo: save(IoTTelemetry com histórico bruto)
    
    IoTService-->>API: TelemetryResponseDTO (instrucaoLed: true, modoLuz: CERRACAO, pwm: 255)
    API-->>ESP32: HTTP 200 OK { instrucaoLed: true, modoLuz: "ALTA_VISIBILIDADE_CERRACAO", intensidadePwm: 255 }
    
    ESP32->>LED: ledcWrite(PWM_CH_RED, 255); ledcWrite(PWM_CH_GREEN, 140); (Cor Âmbar)
    
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
