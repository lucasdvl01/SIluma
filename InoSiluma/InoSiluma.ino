#include <ArduinoJson.h>

// --- Definição dos Pinos ---
const int pinoLDR = A7;          // Leitura do divisor de tensão (LDR + 10k)
const int pinoBotao = 3;        // Botão usando PULLUP interno
const int pinoLedVermelho = 51;  // LED Vermelho
const int pinoLedVerde = 49;     // LED Verde
const int pinoLedAmarelo = 47;   // LED Amarelo

// --- Identificação da Torre ---
const char* codigoTorre = "TR-001";
float nivelBateria = 92.5;
bool ledLigado = false;
unsigned long ultimaTelemetria = 0;
const unsigned long intervaloTelemetria = 3000;

void setup() {
  Serial.begin(115200);
  Serial.setTimeout(100);

  // Configuração dos Pinos
  pinMode(pinoBotao, INPUT_PULLUP);
  pinMode(pinoLedVermelho, OUTPUT);
  pinMode(pinoLedVerde, OUTPUT);
  pinMode(pinoLedAmarelo, OUTPUT);

  // Teste inicial dos LEDs
  atualizarLeds(false, true, false);  // Liga apenas Verde
  delay(500);
  atualizarLeds(false, false, true);  // Liga apenas Amarelo
  delay(500);
  atualizarLeds(true, false, false);  // Liga apenas Vermelho
  delay(500);
  atualizarLeds(false, false, false); // Apaga todos
}

void loop() {
  processarComandosDoBackend();

  int luminosidade = analogRead(pinoLDR);
  bool botaoPressionado = (digitalRead(pinoBotao) == LOW); // LOW quando pressionado

  if (millis() - ultimaTelemetria >= intervaloTelemetria) {
    ultimaTelemetria = millis();

    StaticJsonDocument<200> doc;
    doc["codigoTorre"] = codigoTorre;
    doc["luminosidadeLdr"] = luminosidade;
    doc["statusLed"] = ledLigado;
    doc["nivelBateria"] = nivelBateria;
    doc["pressaoBotaoCerracao"] = botaoPressionado;

    // Uma linha por mensagem permite à ponte separar telemetria e comandos.
    serializeJson(doc, Serial);
    Serial.println();
  }

  delay(20);
}

void processarComandosDoBackend() {
  while (Serial.available() > 0) {
    String linha = Serial.readStringUntil('\n');
    linha.trim();

    if (!linha.startsWith("{")) {
      continue;
    }

    StaticJsonDocument<96> resposta;
    DeserializationError erro = deserializeJson(resposta, linha);
    if (erro) {
      Serial.print(F("[ERRO] Comando JSON invalido: "));
      Serial.println(erro.c_str());
      continue;
    }

    bool alertaAtivado = resposta["alertaAtivado"] | false;
    ledLigado = resposta["instrucaoLed"] | false;

    Serial.print(F("[COMANDO RECEBIDO] alerta="));
    Serial.print(alertaAtivado ? F("true") : F("false"));
    Serial.print(F(", instrucaoLed="));
    Serial.println(ledLigado ? F("true") : F("false"));

    if (alertaAtivado) {
      atualizarLeds(false, false, true);
    } else if (ledLigado) {
      atualizarLeds(false, true, false);
    } else {
      atualizarLeds(false, false, false);
    }
  }
}

void atualizarLeds(bool vermelho, bool verde, bool amarelo) {
  digitalWrite(pinoLedVermelho, vermelho ? HIGH : LOW);
  digitalWrite(pinoLedVerde, verde ? HIGH : LOW);
  digitalWrite(pinoLedAmarelo, amarelo ? HIGH : LOW);
}