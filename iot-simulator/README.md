# Extensão IoT — Simulação e integração com Arduino Mega
**Projeto:** Sistema de Gestão e Monitoramento de Iluminação Inteligente para Minas  
**Instituição:** SENAI Betim (CETEM Maria Madalena Nogueira)  
**Requisitos Atendidos:** RF06, RNF-C10, Item 9 do Documento Oficial do SENAI

Para a lista de materiais, pinagem, segurança e checklist de testes, consulte o
[Guia do circuito Arduino Mega](../docs/GUIA_MONTAGEM_ARDUINO_MEGA_SI_LUMA.md).
O Mega transmite JSON pela serial USB; o script
`serial_http_bridge.ps1` encaminha os dados ao backend local e retorna o comando
para os LEDs. Feche o Monitor Serial antes de iniciar a ponte, pois ela precisa
abrir a mesma porta COM.

---

## 1. Visão Geral da Solução IoT

O subsistema IoT simula uma **Torre de Iluminação Móvel Inteligente** operando em campo nas frentes de lavra e estradas da mina.

No circuito físico, o Arduino Mega lê o LDR e o botão. A ponte no computador
encaminha a telemetria ao backend Spring Boot, que analisa o evento, atualiza o
estado da torre e retorna à ponte a cor sugerida para os LEDs discretos. Os
simuladores Web e Node.js permitem testar esse fluxo sem a placa.

---

## 2. Opções de Execução

Você pode testar a integração IoT de **3 formas diferentes**:

### Opção A: Simulador Web Integrado no Frontend (Mais Fácil)
1. Acesse o sistema web no navegador.
2. No menu lateral, clique em **"Bancada IoT (Simulador)"**.
3. A bancada virtual permite variar o LDR, acionar o botão, ajustar a bateria demonstrativa e observar a resposta da API sem hardware conectado.

---

### Opção B: Simulador Standalone em Linha de Comando (Node.js)
Caso queira gerar fluxo contínuo de telemetria de 5 torres simultaneamente em background:
```bash
node iot-simulator/simulator_runner.js
```
O script transmitirá dados para o backend a cada 2.5 segundos e exibirá as respostas HTTP do servidor.

---

### Opção C: Arduino Mega 2560 conectado por USB
1. Grave `InoSiluma/InoSiluma.ino` na placa e confirme a porta COM no Gerenciador
   de Dispositivos.
2. Inicie o backend local na porta 8080.
3. Feche o Monitor Serial e execute na raiz do repositório:
   ```powershell
   .\iot-simulator\serial_http_bridge.ps1 -Port COM6
   ```
   Substitua `COM6` pela porta identificada no seu computador. A ponte envia
   uma amostra a cada 3 segundos e registra no terminal a decisão do backend.
   Pressione `Ctrl+C` para parar e liberar a porta serial.

---

## 3. Especificação do Payload JSON (RF06)

### Telemetria enviada pela ponte ao backend:
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

### Resposta do backend para a ponte:
- **Código:** `200 OK`
- **Payload:**
```json
{
  "codigoTorre": "TR-001",
  "instrucaoLed": true,
  "modoLuz": "ALTA_VISIBILIDADE_CERRACAO",
  "intensidadePwm": 255,
  "corLedSugerida": "AMBAR",
  "alertaAtivado": true,
  "mensagem": "Alerta de Baixa Visibilidade! Instrução IoT: Ligar LED em potência máxima (Cerração).",
  "horarioServidor": "2026-10-16T10:45:00"
}
```
Na serial, a ponte converte `corLedSugerida` em um caractere por linha: `V`
(verde), `A` (amarelo), `R` (vermelho), `Z` (modo econômico, representado por
amarelo na montagem) ou `O` (apagado).
