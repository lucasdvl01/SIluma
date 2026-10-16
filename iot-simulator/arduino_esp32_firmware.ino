/*
  =====================================================================================
  PROJETO: Sistema de Gestão e Monitoramento de Iluminação Inteligente para Minas
  INSTITUIÇÃO: SENAI Betim (CETEM Maria Madalena Nogueira)
  FIRMWARE: ESP32 com Wi-Fi + Sensor LDR + LED RGB + Botão de Cerração (IoT)
  REQUISITOS ATENDIDOS: RF06, RNF-C10, Item 9 do Documento SENAI
  COMPATIBILIDADE: Simulador Wokwi (ESP32) ou Placa Física ESP32 DevKit v1
  =====================================================================================
*/

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// ==========================================
// CONFIGURAÇÕES DE REDE E SERVIDOR
// ==========================================
// Para simulação no Wokwi use "Wokwi-GUEST" sem senha
// Para teste físico local, insira seu SSID e senha do Wi-Fi
const char* ssid = "Wokwi-GUEST";
const char* password = "";

// URL do Backend Spring Boot
// No Wokwi / Rede Local: use o IP da sua máquina ou localhost com Wokwi Gateway
// Exemplo: "http://192.168.1.100:8080/api/iot"
const char* serverBaseUrl = "http://10.0.2.2:8080/api/iot"; // ou http://localhost:8080/api/iot
const char* CODIGO_TORRE = "TR-001";

// ==========================================
// MAPEAMENTO DE PINOS (GPIO)
// ==========================================
const int PIN_LDR = 34;            // Sensor LDR no pino analógico ADC1_CH6 (GPIO 34)
const int PIN_BOTAO_CERRACAO = 14; // Botão de acionamento manual de cerração (Pull-Up)
const int PIN_LED_RED = 25;        // Canal Vermelho do LED RGB (PWM)
const int PIN_LED_GREEN = 26;      // Canal Verde do LED RGB (PWM)
const int PIN_LED_BLUE = 27;       // Canal Azul do LED RGB (PWM)

// Canais PWM para ESP32
const int PWM_CH_RED = 0;
const int PWM_CH_GREEN = 1;
const int PWM_CH_BLUE = 2;
const int PWM_FREQ = 5000;
const int PWM_RESOLUTION = 8; // 0 - 255

// ==========================================
// VARIÁVEIS DE ESTADO
// ==========================================
unsigned long lastTelemetryTime = 0;
const unsigned long TELEMETRY_INTERVAL = 3000; // Envia a cada 3 segundos
bool ledLuminariaLigada = false;
String modoAtual = "NORMAL";
float nivelBateriaSimulado = 92.5;

void setup() {
  Serial.begin(115200);
  delay(1000);
  Serial.println("\n=======================================================");
  Serial.println("  SENAI Betim - Iluminacao Inteligente para Minas IoT  ");
  Serial.println("=======================================================");
  Serial.print("Torre: ");
  Serial.println(CODIGO_TORRE);

  // Configuração dos Pinos
  pinMode(PIN_LDR, INPUT);
  pinMode(PIN_BOTAO_CERRACAO, INPUT_PULLUP);

  // Configuração do PWM para o LED RGB
  ledcSetup(PWM_CH_RED, PWM_FREQ, PWM_RESOLUTION);
  ledcSetup(PWM_CH_GREEN, PWM_FREQ, PWM_RESOLUTION);
  ledcSetup(PWM_CH_BLUE, PWM_FREQ, PWM_RESOLUTION);

  ledcAttachPin(PIN_LED_RED, PWM_CH_RED);
  ledcAttachPin(PIN_LED_GREEN, PWM_CH_GREEN);
  ledcAttachPin(PIN_LED_BLUE, PWM_CH_BLUE);

  // Teste inicial das cores do LED (Verde -> Âmbar -> Azul -> Desliga)
  setCorRGB(0, 255, 0); delay(400);   // Verde (Normal)
  setCorRGB(255, 120, 0); delay(400); // Âmbar (Cerração)
  setCorRGB(0, 100, 255); delay(400); // Azul (Eco)
  setCorRGB(0, 0, 0);                 // Apagado

  // Conexão Wi-Fi
  conectarWiFi();
}

void loop() {
  // Mantém conexão Wi-Fi ativa
  if (WiFi.status() != WL_CONNECTED) {
    conectarWiFi();
  }

  // Verifica se o botão de cerração foi pressionado em campo
  bool botaoPressionado = (digitalRead(PIN_BOTAO_CERRACAO) == LOW);

  // Envio periódico de telemetria ou imediato se o botão for pressionado
  if (millis() - lastTelemetryTime >= TELEMETRY_INTERVAL || botaoPressionado) {
    lastTelemetryTime = millis();
    enviarTelemetria(botaoPressionado);
    if (botaoPressionado) {
      delay(500); // Debounce
    }
  }

  delay(50);
}

void conectarWiFi() {
  Serial.print("Conectando ao Wi-Fi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  int tentativas = 0;
  while (WiFi.status() != WL_CONNECTED && tentativas < 20) {
    delay(500);
    Serial.print(".");
    tentativas++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n[Wi-Fi] Conectado com sucesso!");
    Serial.print("[Wi-Fi] IP obtido: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\n[Wi-Fi] Falha ao conectar. Operando em modo autônomo local.");
  }
}

void enviarTelemetria(bool botaoCerracaoPressionado) {
  // Leitura do Sensor LDR (0 a 4095 no ESP32, normalizamos para escala 0 a 1023)
  int leituraRaw = analogRead(PIN_LDR);
  int leituraLdr = map(leituraRaw, 0, 4095, 0, 1023);

  // Simulação de descarga suave de bateria
  if (ledLuminariaLigada) {
    nivelBateriaSimulado -= 0.02;
    if (nivelBateriaSimulado < 15.0) nivelBateriaSimulado = 100.0; // Recarrega
  }

  Serial.println("\n-------------------------------------------------------");
  Serial.printf("[SENSOR LDR] Valor medido: %d | Bateria: %.1f%%\n", leituraLdr, nivelBateriaSimulado);
  if (botaoCerracaoPressionado) {
    Serial.println("[BOTAO] Botao manual de CERRACAO acionado!");
  }

  if (WiFi.status() != WL_CONNECTED) {
    // Automação local de fallback caso sem rede
    if (leituraLdr < 300 || botaoCerracaoPressionado) {
      Serial.println("[FALLBACK OFFLINE] Baixa luminosidade detectada! Acionando LED Cerração localmente.");
      setCorRGB(255, 120, 0); // Âmbar cerração
    }
    return;
  }

  HTTPClient http;
  String url = String(serverBaseUrl) + "/telemetry";
  http.begin(url);
  http.addHeader("Content-Type", "application/json");

  // Criação do payload JSON
  StaticJsonDocument<256> doc;
  doc["codigoTorre"] = CODIGO_TORRE;
  doc["luminosidadeLdr"] = leituraLdr;
  doc["statusLed"] = ledLuminariaLigada;
  doc["nivelBateria"] = round(nivelBateriaSimulado * 10) / 10.0;
  doc["pressaoBotaoCerracao"] = botaoCerracaoPressionado;

  String jsonPayload;
  serializeJson(doc, jsonPayload);

  Serial.print("[HTTP POST] Enviando telemetria: ");
  Serial.println(jsonPayload);

  int httpResponseCode = http.POST(jsonPayload);

  if (httpResponseCode == 200) {
    String responseString = http.getString();
    Serial.print("[HTTP 200] Resposta do Servidor: ");
    Serial.println(responseString);

    StaticJsonDocument<512> responseDoc;
    DeserializationError error = deserializeJson(responseDoc, responseString);

    if (!error) {
      bool instrucaoLed = responseDoc["instrucaoLed"];
      const char* modoLuz = responseDoc["modoLuz"];
      int pwm = responseDoc["intensidadePwm"];
      bool alertaAtivado = responseDoc["alertaAtivado"];

      ledLuminariaLigada = instrucaoLed;
      modoAtual = String(modoLuz);

      Serial.printf("[ACAO] LED: %s | Modo: %s | Alerta: %s\n",
                    instrucaoLed ? "LIGADO" : "DESLIGADO",
                    modoLuz,
                    alertaAtivado ? "SIM (CERRACAO)" : "NAO");

      // Atualiza a cor física/virtual do LED RGB
      if (!instrucaoLed) {
        setCorRGB(0, 0, 0); // Apaga luminária
      } else if (String(modoLuz) == "ALTA_VISIBILIDADE_CERRACAO" || alertaAtivado) {
        setCorRGB(255, 140, 0); // Cor Âmbar / Alta visibilidade contra cerração
      } else if (String(modoLuz) == "ECONOMIA") {
        setCorRGB(0, 120, 255); // Azul econômico
      } else {
        setCorRGB(0, 255, 0);   // Verde operacional padrão
      }
    }
  } else {
    Serial.printf("[HTTP ERRO] Falha na comunicacao com backend. Codigo: %d\n", httpResponseCode);
  }

  http.end();
}

void setCorRGB(int r, int g, int b) {
  ledcWrite(PWM_CH_RED, r);
  ledcWrite(PWM_CH_GREEN, g);
  ledcWrite(PWM_CH_BLUE, b);
}
