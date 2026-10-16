# Extensão IoT - Simulação e Integração com Arduino / ESP32
**Projeto:** Sistema de Gestão e Monitoramento de Iluminação Inteligente para Minas  
**Instituição:** SENAI Betim (CETEM Maria Madalena Nogueira)  
**Requisitos Atendidos:** RF06, RNF-C10, Item 9 do Documento Oficial do SENAI

---

## 1. Visão Geral da Solução IoT

O subsistema IoT simula uma **Torre de Iluminação Móvel Inteligente** operando em campo nas frentes de lavra e estradas da mina.

O dispositivo monitora as condições ambientais (queda de luminosidade provocada por cerração, neblina ou período noturno) através do sensor LDR ou de um botão manual de cerração. Ao detectar baixa luminosidade, o firmware envia uma requisição `POST /api/iot/telemetry` para o backend Spring Boot. O backend analisa o evento, altera o status da torre para **"Alerta de Baixa Visibilidade"** e responde com a instrução de acionamento imediato do LED RGB na cor de alta visibilidade (Âmbar/Amarelo).

---

## 2. Opções de Execução

Você pode testar a integração IoT de **3 formas diferentes**:

### Opção A: Simulador Web Integrado no Frontend (Mais Fácil)
1. Acesse o sistema web no navegador.
2. No menu lateral, clique em **"Simulador IoT"**.
3. Você verá uma bancada virtual completa com a placa ESP32, slider do sensor LDR, botão de cerração, medidor de bateria e o LED RGB respondendo instantaneamente em tempo real à API REST!

---

### Opção B: Simulador Standalone em Linha de Comando (Node.js)
Caso queira gerar fluxo contínuo de telemetria de 5 torres simultaneamente em background:
```bash
node iot-simulator/simulator_runner.js
```
O script transmitirá dados para o backend a cada 2.5 segundos e exibirá as respostas HTTP do servidor.

---

### Opção C: Simulador Wokwi (Navegador com ESP32 Virtual) ou Hardware Físico
#### 1. Pinagem no ESP32:
| Componente | Pino no ESP32 | Descrição |
| :--- | :--- | :--- |
| **Sensor LDR (AO)** | `GPIO 34` | Entrada analógica ADC1 para medição de luz |
| **Botão Cerração** | `GPIO 14` | Entrada digital com resistor Pull-Up interno |
| **LED RGB - Vermelho** | `GPIO 25` | Canal PWM para controle de intensidade |
| **LED RGB - Verde** | `GPIO 26` | Canal PWM |
| **LED RGB - Azul** | `GPIO 27` | Canal PWM |
| **Alimentação LDR/LED** | `3V3` e `GND` | Alimentação lógica 3.3V |

#### 2. Executando no Wokwi:
1. Acesse [https://wokwi.com](https://wokwi.com) e crie um novo projeto **ESP32**.
2. Copie o conteúdo de `iot-simulator/arduino_esp32_firmware.ino` para a aba de código (`sketch.ino`).
3. Copie o conteúdo de `iot-simulator/wokwi_diagram.json` para a aba `diagram.json`.
4. Inicie a simulação no Wokwi!

---

## 3. Especificação do Payload JSON (RF06)

### Requisição do Dispositivo para o Backend:
- **Método:** `POST`
- **URL:** `http://localhost:8080/api/iot/telemetry`
- **Headers:** `Content-Type: application/json`
- **Payload:**
```json
{
  "codigoTorre": "TR-001",
  "luminosidadeLdr": 180,
  "statusLed": false,
  "nivelBateria": 85.5,
  "pressaoBotaoCerracao": false
}
```

### Resposta do Backend para o Dispositivo:
- **Código:** `200 OK`
- **Payload:**
```json
{
  "codigoTorre": "TR-001",
  "instrucaoLed": true,
  "modoLuz": "ALTA_VISIBILIDADE_CERRACAO",
  "intensidadePwm": 255,
  "alertaAtivado": true,
  "mensagem": "Alerta de Baixa Visibilidade! Instrução IoT: Ligar LED em potência máxima (Cerração).",
  "horarioServidor": "2026-10-16T10:45:00"
}
```
