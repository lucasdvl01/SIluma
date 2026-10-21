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
char bufferComando[8];
size_t tamanhoComando = 0;
bool descartandoComando = false;

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
    char caractere = static_cast<char>(Serial.read());
    if (caractere == '\r') {
      continue;
    }

    if (caractere != '\n') {
      if (descartandoComando) {
        continue;
      }
      if (tamanhoComando >= sizeof(bufferComando) - 1) {
        tamanhoComando = 0;
        descartandoComando = true;
        continue;
      }
      bufferComando[tamanhoComando++] = caractere;
      continue;
    }

    if (descartandoComando) {
      descartandoComando = false;
      tamanhoComando = 0;
      Serial.println(F("[ERRO] Comando serial excedeu o limite"));
      continue;
    }

    if (tamanhoComando != 1) {
      tamanhoComando = 0;
      Serial.println(F("[ERRO] Comando serial desconhecido"));
      continue;
    }

    char comando = bufferComando[0];
    tamanhoComando = 0;
    ledLigado = comando != 'O';

    if (comando == 'R') {
      atualizarLeds(true, false, false);
    } else if (comando == 'A' || comando == 'Z') {
      atualizarLeds(false, false, true);
    } else if (comando == 'V') {
      atualizarLeds(false, true, false);
    } else if (comando == 'O') {
      atualizarLeds(false, false, false);
    } else {
      ledLigado = false;
      Serial.println(F("[ERRO] Codigo de comando serial invalido"));
      continue;
    }

    Serial.print(F("[COMANDO RECEBIDO] codigo="));
    Serial.println(comando);
  }
}

void atualizarLeds(bool vermelho, bool verde, bool amarelo) {
  digitalWrite(pinoLedVermelho, vermelho ? HIGH : LOW);
  digitalWrite(pinoLedVerde, verde ? HIGH : LOW);
  digitalWrite(pinoLedAmarelo, amarelo ? HIGH : LOW);
}